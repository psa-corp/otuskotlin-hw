import torch
import requests
import json
import re 
import time
from fastapi import FastAPI, Body, HTTPException
from typing import List, Optional
from PIL import Image
from io import BytesIO
from transformers import Qwen3VLForConditionalGeneration, AutoProcessor, BitsAndBytesConfig
from enum import Enum
from pydantic import BaseModel
from json_repair import repair_json

# "__________ FastAPI __________"

HF_TOKEN = ""
app = FastAPI(title="Qwen3 Ingredient Scanner")

bnb_config = BitsAndBytesConfig(
    load_in_8bit=True,
    llm_int8_threshold=6.0,
    llm_int8_skip_modules=["visual", "lm_head"],
    torch_dtype=torch.float16
)

MODEL_ID = "Qwen/Qwen3-VL-4B-Instruct"

print(f"--- Loading {MODEL_ID} (Universal AutoModel mode) ---")

model = Qwen3VLForConditionalGeneration.from_pretrained(
    MODEL_ID,
    quantization_config=bnb_config,
    device_map="auto",
    trust_remote_code=True,
    attn_implementation="sdpa",
    torch_dtype=torch.float16,
    token=HF_TOKEN
)

processor = AutoProcessor.from_pretrained(MODEL_ID, trust_remote_code=True, token=HF_TOKEN)
print(f"Model device: {next(model.parameters()).device}")
print(f"--- VRAM after model load ---")
print(f"Allocated: {torch.cuda.memory_allocated()/1024**3:.2f} GB")
print(f"Reserved:  {torch.cuda.memory_reserved()/1024**3:.2f} GB")
print(f"Free (approx): {(torch.cuda.get_device_properties(0).total_memory - torch.cuda.memory_reserved())/1024**3:.2f} GB")

# "__________ ENUMS __________"
class IsRiskLevel(str, Enum):
    NONE = "NONE"
    MINIMAL = "MINIMAL"
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"

class IsColor(str, Enum):
    NONE = "NONE"
    VERY_DARK_RED = "VERY_DARK_RED"
    DARK_RED = "DARK_RED"
    DEEP_RED = "DEEP_RED"
    RED = "RED"
    LIGHT_RED = "LIGHT_RED"
    RED_ORANGE = "RED_ORANGE"
    ORANGE = "ORANGE"
    LIGHT_ORANGE = "LIGHT_ORANGE"
    DARK_YELLOW = "DARK_YELLOW"
    YELLOW = "YELLOW"
    LIGHT_YELLOW = "LIGHT_YELLOW"
    YELLOW_GREEN = "YELLOW_GREEN"
    PALE_GREEN = "PALE_GREEN"
    LIGHT_GREEN = "LIGHT_GREEN"
    GREEN = "GREEN"
    MEDIUM_GREEN = "MEDIUM_GREEN"
    BRIGHT_GREEN = "BRIGHT_GREEN"
    VIBRANT_GREEN = "VIBRANT_GREEN"
    FRESH_GREEN = "FRESH_GREEN"
    BRILLIANT_GREEN = "BRILLIANT_GREEN"

class IsLogLevel(str, Enum):
    DEBUG = "DEBUG"
    INFO = "INFO"
    WARNING = "WARNING"
    ERROR = "ERROR"

class IsError(BaseModel):
    code: str = ""
    group: str = ""
    field: str = ""
    message: str = ""
    level: IsLogLevel = IsLogLevel.ERROR
    exception: Optional[str] = None  

class Component(BaseModel):
    name: str = ""
    scientific_name: str = ""
    description: str = ""
    sources: str = ""
    risk_level: str = ""
    health_risks: str = ""

class AiAnalysis(BaseModel):
    description: str = ""
    rating: float = -1.0
    color: IsColor = IsColor.NONE
    components: List[Component] = []
    errors: List[IsError] = []

    def is_empty(self) -> bool:
        return self == AiAnalysis()

    class Config:
        frozen = False

AI_ANALYSIS_NONE = AiAnalysis()

# "__________ API __________"
def rating_to_color(rating: float) -> IsColor:
    if rating < 0 or rating > 10:
        return IsColor.NONE
        
    idx = int(rating // 0.5)
    if idx >= 20:
        idx = 19  
    colors = [
        IsColor.VERY_DARK_RED,
        IsColor.DARK_RED,
        IsColor.DEEP_RED,
        IsColor.RED,
        IsColor.LIGHT_RED,
        IsColor.RED_ORANGE,
        IsColor.ORANGE,
        IsColor.LIGHT_ORANGE,
        IsColor.DARK_YELLOW,
        IsColor.YELLOW,
        IsColor.LIGHT_YELLOW,
        IsColor.YELLOW_GREEN,
        IsColor.PALE_GREEN,
        IsColor.LIGHT_GREEN,
        IsColor.GREEN,
        IsColor.MEDIUM_GREEN,
        IsColor.BRIGHT_GREEN,
        IsColor.VIBRANT_GREEN,
        IsColor.FRESH_GREEN,
        IsColor.BRILLIANT_GREEN,
    ]
    return colors[idx]

@app.post("/v1/ai/analyze", response_model=AiAnalysis)
async def analyze_safety(composition: str = Body(..., embed=True)):
    torch.cuda.empty_cache()
    start = time.time()

    prompt = f"""
    Ты — эксперт по анализу состава продуктов питания. Твоя задача — вернуть ТОЛЬКО валидный JSON без пояснений, комментариев или markdown-разметки. Используй строгий синтаксис JSON: после каждого элемента объекта обязательно ставь запятую, кроме последнего.

    Проанализируй следующий список ингредиентов: {composition}.

    Оцени безопасность и полезность продукта по шкале от 0.0 (очень опасно) до 10.0 (полностью безопасно и полезно).

    Напиши подробное описание продукта и каждого компонента: для каждого компонента укажи название, научное название, описание, источники (где встречается), уровень риска (NONE, MINIMAL, LOW, MEDIUM, HIGH, CRITICAL) и возможные риски для здоровья.

    Всю информацию представь в виде JSON со следующими полями:
    - rating (число от 0.0 до 10.0)
    - description (строка с подробным текстовым анализом)
    - components (список объектов, каждый с полями: name, scientific_name, description, sources, risk_level, health_risks)

    Пример структуры:
    {{
    "rating": 7.5,
    "description": "Продукт содержит подсластители и регуляторы кислотности. В целом умеренно безопасен.",
    "components": [
        {{
        "name": "Аспартам",
        "scientific_name": "E951",
        "description": "Искусственный подсластитель",
        "sources": "Диетические напитки, жевательная резинка",
        "risk_level": "MEDIUM",
        "health_risks": "Фенилкетонурия, возможные неврологические эффекты при чрезмерном употреблении"
        }}
    ]
    }}

    Не добавляй ничего, кроме JSON. Убедись, что JSON корректен.
    """

    messages = [{"role": "user", "content": [{"type": "text", "text": prompt}]}]
    text = processor.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
    inputs = processor(text=[text], return_tensors="pt").to("cuda")

    print(f"VRAM before generate: {torch.cuda.memory_allocated()/1024**3:.2f} GB") 
    print(f"Input shape: {inputs.input_ids.shape}")
    
    torch.cuda.synchronize()  # замерить чистое время GPU
    start_gen = time.time()

    with torch.no_grad():
        output = model.generate(
            **inputs,
            use_cache=True,
            do_sample=False,
            pad_token_id=processor.tokenizer.pad_token_id,
            max_new_tokens=2048
        )

    torch.cuda.synchronize()
    end_gen = time.time()
    print(f"Generate took {end_gen - start_gen:.2f} seconds")

    print(f"VRAM after generate: {torch.cuda.memory_allocated()/1024**3:.2f} GB")
    generated_ids = [
        output_ids[len(input_ids):] for input_ids, output_ids in zip(inputs.input_ids, output)
    ]
    response_text = processor.batch_decode(generated_ids, skip_special_tokens=True)[0].strip()
 
    print("=== Сырой ответ модели ===")
    print(response_text)
    print("===========================")

    json_match = re.search(r'\{.*\}', response_text, re.DOTALL)
    if not json_match:
        error = IsError(
            code="PARSE_ERROR",
            message="Не удалось распарсить ответ модели как JSON",
            level=IsLogLevel.ERROR
        )
        return AiAnalysis(errors=[error])

    try:
        data = json.loads(json_match.group())
    except json.JSONDecodeError as e:
        try:
            repaired_json_str = repair_json(json_match.group(), return_objects=False)
            data = json.loads(repaired_json_str)
            print("JSON успешно восстановлен")
        except Exception as repair_error:
            error = IsError(
                code="JSON_DECODE_ERROR",
                message=f"Ошибка декодирования JSON даже после восстановления: {repair_error}",
                level=IsLogLevel.ERROR
            )
            end = time.time()
            print(f"Request took {end - start:.2f} seconds")
            print("=== Сырой ответ модели ===")
            print(response_text)
            print("===========================")
            return AiAnalysis(errors=[error])


 
    components_data = data.get("components", [])
    components_list = []
    for comp in components_data:
        component = Component(
            name=comp.get("name", ""),
            scientific_name=comp.get("scientific_name", ""),
            description=comp.get("description", ""),
            sources=comp.get("sources", ""),
            risk_level=comp.get("risk_level", "NONE"),
            health_risks=comp.get("health_risks", "")
        )
        components_list.append(component)

    description = data.get("description", "").strip()

    # Валидация полей
    rating = data.get("rating", -1.0)
    if not isinstance(rating, (int, float)) or rating < 0 or rating > 10:
        rating = -1.0

    color_str = data.get("color", "NONE")
    color = rating_to_color(rating) if rating >= 0 else IsColor.NONE
 
    result = AiAnalysis(
        description=description,
        rating=rating,
        color=color,
        components=components_list,
        errors=[]
    )

    end = time.time()
    print(f"Request took {end - start:.2f} seconds")
    return result

def resize_for_vram(img: Image.Image):
    if img.mode != "RGB": img = img.convert("RGB")
    img.thumbnail((1024, 1024), Image.Resampling.LANCZOS)
    return img

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

    cuda_available = torch.cuda.is_available()
    device_name = torch.cuda.get_device_name(0) if cuda_available else "CPU"

    print(f"--- Проверка железа ---")
    print(f"CUDA available: {cuda_available}")
    print(f"Current device: {device_name}")
    print(f"Модель: {MODEL_ID}")
    print(f"-----------------------")

    uvicorn.run(app, host="0.0.0.0", port=5000)
