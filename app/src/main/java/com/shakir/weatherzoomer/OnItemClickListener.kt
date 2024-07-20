package com.shakir.weatherzoomer

interface OnItemClickListener<T, D> {
    fun onItemClick(item: T,enum: D, position: Int?)
}
