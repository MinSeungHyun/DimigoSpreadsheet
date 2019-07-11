package com.seunghyun.dimigospreadsheet.utils

import org.json.JSONArray
import org.json.JSONObject

class JSONParser {
    companion object {
        fun parse(json: String, key: String): String {
            return JSONObject(json).getString(key)
        }

        fun parseFromArray(jsonArray: String, index: Int, key: String): String {
            return JSONObject(JSONArray(jsonArray).get(index).toString()).getString(key)
        }
    }
}