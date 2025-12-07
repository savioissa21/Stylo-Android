package com.example.styloandroid.data.model

data class Service(
    val id: String = "",
    val name: String = "",        
    val description: String = "",  
    val price: Double = 0.0,       
    val durationMin: Int = 30,    
    val employeeIds: List<String> = emptyList() 
)