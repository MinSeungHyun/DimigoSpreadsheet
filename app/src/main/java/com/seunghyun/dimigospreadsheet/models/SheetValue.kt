package com.seunghyun.dimigospreadsheet.models

class SheetValue(private val sheetValues: List<List<Any>>?) {
    fun getTotalCount(): String {
        return sheetValues?.get(1)?.get(1).toString()
    }

    fun getVacancyCount(): String {
        return sheetValues?.get(2)?.get(1).toString()
    }

    fun getCurrentCount(): String {
        return sheetValues?.get(3)?.get(1).toString()
    }

    fun getIngang1(): ArrayList<String> {
        return getColumnValues(2, 1)
    }

    fun getIngang2(): ArrayList<String> {
        return getColumnValues(3, 1)
    }

    fun getClub(): ArrayList<String> {
        return getColumnValues(4, 1)
    }

    fun getEtc(): ArrayList<String> {
        return getColumnValues(5, 1)
    }

    fun getBathroom(): ArrayList<String> {
        return getColumnValues(0, 9)
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