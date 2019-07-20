package com.seunghyun.dimigospreadsheet.models

class ArrayListDifference(list1: ArrayList<String>, list2: ArrayList<String>) {
    val addedList = ArrayList<String>()
    val deletedList = ArrayList<String>()

    //list1에서 list2로 추가된 값, 삭제된 값을 구함
    init {
        addedList.addAll(list2)
        deletedList.addAll(list1)

        addedList.removeAll(list1)
        deletedList.removeAll(list2)
    }
}