package com.google.firebase.example.makeitso.data.injection

import com.google.firebase.example.makeitso.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseHiltModule {
    @Provides
    @Singleton
    fun supabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Functions)
    }

    @Provides fun auth(client: SupabaseClient): Auth = client.auth

    @Provides fun postgrest(client: SupabaseClient): Postgrest = client.postgrest

    @Provides fun functions(client: SupabaseClient): Functions = client.functions
}
