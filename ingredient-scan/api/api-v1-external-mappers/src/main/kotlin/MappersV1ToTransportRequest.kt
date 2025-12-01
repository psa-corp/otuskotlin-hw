package net.otuskotlin.ingredientscan.mappers.v1

import net.otuskotlin.ingredientscan.api.v1.external.models.ScanManualDto
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanPhotosDto
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanType
import net.otuskotlin.ingredientscan.core.common.models.IsScan
import net.otuskotlin.ingredientscan.core.common.models.IsScanId

fun IsScan.toTransportCreateManual() = ScanManualDto(
    id = id.takeIf { it != IsScanId.NONE }?.asString(),
    type = ScanType.MANUAL,
    text = "томаты измельченные 72%, концентрат томатный 14%, лук, масло подсолнечное рафинированное, базилик 2%, сахар, соль, регулятор кислотности."
)

fun IsScan.toTransportCreatePhotos() = ScanPhotosDto(
    id = id.takeIf { it != IsScanId.NONE }?.asString(),
    type = ScanType.PHOTO
)