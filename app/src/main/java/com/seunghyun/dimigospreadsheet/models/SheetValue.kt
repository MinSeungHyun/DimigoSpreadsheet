package com.seunghyun.dimigospreadsheet.models

class SheetValue(private val sheetValues: List<List<Any>>?) {
    val totalCount by lazy { getCount(1) }
    val vacancyCount by lazy { getCount(2) }
    val currentCount by lazy { getCount(3) }

    val ingang1 by lazy { getColumnValues(2, 1) }
    val ingang2 by lazy { getColumnValues(3, 1) }
    val club by lazy { getColumnValues(4, 1) }
    val etc by lazy { getColumnValues(5, 1) }
    val bathroom by lazy { getColumnValues(0, 9) }

    private fun getCount(position: Int): String {
        return try {
            sheetValues?.get(position)?.get(1).toString()
        } catch (e: Exception) {
            "0"
        }
    }

    private fun getColumnValues(column: Int, minColumn: Int): ArrayList<String> {
        val values = ArrayList<String>()
        sheetValues?.forEach {
            if (sheetValues.indexOf(it) >= minColumn && it.size > column && it[column].toString().isNotBlank())
                values.add(it[column].toString())
        }
        return values
    }
}