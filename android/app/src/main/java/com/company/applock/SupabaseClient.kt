package com.company.applock
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
object Supabase {
    val client = createSupabaseClient(supabaseUrl = "https://oxyaywwccwnpwzoysnha.supabase.co", supabaseKey = "sb_publishable_z82X2pQcOAKLrowMxARR3w_ztloH4Te") { install(Auth); install(Postgrest) }
}
