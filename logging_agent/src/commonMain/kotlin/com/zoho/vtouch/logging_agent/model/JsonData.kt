package com.zoho.vtouch.logging_agent.model

data class  JsonData(val type:Int,val json: Any,val id: String){
    companion object{
         val TABLE_DATA : Int = 1
         val LOG_MESSAGE : Int = 2
         val GRAPH_DATA :Int = 3
         val INITIAL_DATA : Int = 4
    }
}

