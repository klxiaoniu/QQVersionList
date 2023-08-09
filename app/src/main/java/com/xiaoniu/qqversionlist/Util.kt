package com.xiaoniu.qqversionlist

class Util {
    companion object {
        fun String.getVersionBig(): String {
            val regex = Regex("""versionNumber=([^\s,]+)""")
            val vName = regex.find(this)!!.groupValues[1]
            return vName
        }

        fun String.getSize(): String {
            val regex = Regex("""size=([^\s,]+)""")
            val size = regex.find(this)!!.groupValues[1]
            return size
        }
    }
}