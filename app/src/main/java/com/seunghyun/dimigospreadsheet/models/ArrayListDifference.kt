package com.seunghyun.dimigospreadsheet.models

import android.util.SparseArray

//list1에서 어떤값을 삭제하고 어떤값을 추가해야 list2가 되는지 구함 (deleteList => addedList => finallyDeleteList)
class ArrayListDifference(list1: ArrayList<String>, list2: ArrayList<String>) {
    val addedList = SparseArray<String>()
    val deletedList = ArrayList<String>()
    val finallyDeleteList = ArrayList<Int>()

    private val verifyList = ArrayList<String>()

    init {
        verifyList.addAll(list1)
        list1.forEach {
            if (!list2.contains(it)) {
                deletedList.add(it)
                verifyList.remove(it)
            }
        }

        while (verifyList.size < list2.size) {
            verifyList.add("")
        }

        while (!contains(verifyList, list2)) {
            for (i in (0 until list2.size)) {
                if (list2[i] != verifyList[i]) {
                    addedList.put(i, list2[i])
                    verifyList.add(i, list2[i])
                    break
                }
            }
        }

        while (list2.size != verifyList.size) {
            finallyDeleteList.add(list2.size)
            verifyList.removeAt(list2.size)
        }
    }

    private fun contains(big: ArrayList<String>, small: ArrayList<String>): Boolean {
        if (small.size > big.size) return false
        for (i in (0 until small.size)) {
            if (big[i] != small[i]) {
                return false
            }
        }
        return true
    }
}