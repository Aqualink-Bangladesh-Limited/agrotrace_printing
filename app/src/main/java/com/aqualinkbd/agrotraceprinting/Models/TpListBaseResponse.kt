package com.aqualinkbd.agrotraceprinting.Models

data class TpListBaseResponse(
    val `data`: List<TpData>?,
    val success: Boolean?
)