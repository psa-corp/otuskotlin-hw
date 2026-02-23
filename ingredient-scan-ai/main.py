import torch
import requests
from fastapi import FastAPI, Body, HTTPException
from typing import List
from PIL import Image
from io import BytesIO
from transformers import Qwen3VLForConditionalGeneration, AutoProcessor, BitsAndBytesConfig


app = FastAPI(title="Qwen3 Ingredient Scanner")

# 1. Настройка 4-битной квантизации для  RTX 4070 Laptop 8GB
bnb_config = BitsAndBytesConfig(
    load_in_4bit=True,
    bnb_4bit_quant_type="nf4",
    bnb_4bit_use_double_quant=True, # Сохраняет точность при экономии памяти
    bnb_4bit_compute_dtype=torch.bfloat16,# Точнее, чем float16 на 40-й серии
    llm_int8_enable_fp32_cpu_offload=True
)

HF_TOKEN = ""

MODEL_ID = "Qwen/Qwen3-VL-4B-Instruct"

print(f"--- Loading {MODEL_ID} (Universal AutoModel mode) ---")

# 2. Загружаем через базовый AutoModel
model = Qwen3VLForConditionalGeneration.from_pretrained(
    MODEL_ID,
    quantization_config=bnb_config,
    device_map="auto",
    trust_remote_code=True,
    attn_implementation="sdpa",
    token=HF_TOKEN # Не забудь обновить токен, если старый отозвал
)

processor = AutoProcessor.from_pretrained(MODEL_ID, trust_remote_code=True, token=HF_TOKEN)

def resize_for_vram(img: Image.Image):
    if img.mode != "RGB": img = img.convert("RGB")
    img.thumbnail((1024, 1024), Image.Resampling.LANCZOS)
    return img

@app.post("/v1/ai/analyze")
async def analyze_safety(ingredients: str = Body(..., embed=True)):
    prompt = f"Проанализируй состав продукта на безопасность: {ingredients}"
    messages = [{"role": "user", "content": [{"type": "text", "text": prompt}]}]

    text = processor.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
    inputs = processor(text=[text], return_tensors="pt").to("cuda")

    with torch.no_grad():
        output = model.generate(
            **inputs,
            use_cache=True,
            do_sample=False,
            pad_token_id=processor.tokenizer.pad_token_id,
            max_new_tokens=512
        )

    # 4. ВАЖНО: Декодируем весь батч сразу, или берем [0], но убираем входные токены
    # Чтобы не получить на выходе промпт + ответ, используем skip_special_tokens
    generated_ids = [
        output_ids[len(input_ids):] for input_ids, output_ids in zip(inputs.input_ids, output)
    ]
    response = processor.batch_decode(generated_ids, skip_special_tokens=True)[0]
    return {"analysis": response.strip()}

@app.post("/v1/ai/ocr-composition")
async def extract_composition(s3_urls: List[str]):
    images = []
    try:
        for url in s3_urls:
            resp = requests.get(url, timeout=10)
            img = Image.open(BytesIO(resp.content))
            images.append(resize_for_vram(img))
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Ошибка S3: {e}")

    messages = [{
        "role": "user",
        "content": [
            {"type": "text", "text": "Выпиши полный текст состава из этих фото."},
            *[{"type": "image", "image": i} for i in images]
        ]
    }]

    torch.cuda.empty_cache()
    prompt = processor.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
    # 5. ОШИБКА: Для картинок нужно передавать images=images отдельно в процессор
    inputs = processor(text=[prompt], images=images, return_tensors="pt").to("cuda")

    with torch.no_grad():
        output = model.generate(**inputs, max_new_tokens=1024)

    # Срезаем промпт, чтобы оставить только чистый ответ нейронки
    generated_ids = [
        output_ids[len(input_ids):] for input_ids, output_ids in zip(inputs.input_ids, output)
    ]
    result = processor.batch_decode(generated_ids, skip_special_tokens=True)[0]
    return {"composition": result.strip()}

if __name__ == "__main__":
    import uvicorn
    import torch

    cuda_available = torch.cuda.is_available()
    device_name = torch.cuda.get_device_name(0) if cuda_available else "CPU"

    print(f"--- Проверка железа ---")
    print(f"CUDA available: {cuda_available}")
    print(f"Current device: {device_name}")
    print(f"Модель: {MODEL_ID}")
    print(f"-----------------------")

    # Если хочешь автоперезагрузку при правке кода, добавь reload=True
    # Но тогда нужно передать "main:app" строкой
    uvicorn.run(app, host="0.0.0.0", port=5000)
