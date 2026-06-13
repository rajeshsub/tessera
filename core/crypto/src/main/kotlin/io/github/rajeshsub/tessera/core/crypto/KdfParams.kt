package io.github.rajeshsub.tessera.core.crypto

data class KdfParams(
    val mCostKibibytes: Int,
    val tCostIterations: Int,
    val parallelism: Int,
)
