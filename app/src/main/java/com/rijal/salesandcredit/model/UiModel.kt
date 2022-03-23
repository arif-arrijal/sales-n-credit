package com.rijal.salesandcredit.model

object UiModel {
    class DashboardMenu(val menuName: String, val imageId: Int?, val func: () -> Unit)
    data class Search(val id: Int, val title: String, val description: String)
    data class GoodsAdd(var id: Int, var title: String, var description: String, var qty: Int) {
        companion object {
            fun fromSearch(data: Search): GoodsAdd {
                return GoodsAdd(data.id, data.title, data.description, 1)
            }
        }
    }
}