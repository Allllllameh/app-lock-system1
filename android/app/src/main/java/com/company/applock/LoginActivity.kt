package com.company.applock
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.applock.databinding.ActivityLoginBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPass.text.toString().trim()
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) { Toast.makeText(this, "همه فیلدها را پر کنید", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            binding.btnLogin.text = "لطفا صبر کنید..."
            binding.btnLogin.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    Supabase.client.auth.signInWith(Email) { this.email = email; password = pass }
                    val userId = Supabase.client.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        Supabase.client.from("profiles").update(buildJsonObject { put("full_name", name) }) { filter { eq("id", userId) } }
                        val pm = packageManager
                        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                        val appsJson = buildJsonArray { apps.forEach { app -> if (pm.getLaunchIntentForPackage(app.packageName) != null) { add(buildJsonObject { put("appName", pm.getApplicationLabel(app).toString()); put("packageName", app.packageName) }) } } }
                        Supabase.client.from("devices").upsert(buildJsonObject { put("user_id", userId); put("installed_apps", appsJson); put("device_name", android.os.Build.MODEL) })
                        val policyResult = Supabase.client.from("policies").select { filter { eq("user_id", userId) } }.decodeSingleOrNull<JsonObject>()
                        val lockedPackages = policyResult?.get("app_packages")?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                        val prefs = getSharedPreferences("AppLockPrefs", MODE_PRIVATE)
                        prefs.edit().putStringSet("locked_packages", lockedPackages.toSet()).apply()
                        Toast.makeText(this@LoginActivity, "ورود موفق!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java)); finish()
                    }
                } catch (e: Exception) {
                    binding.btnLogin.text = "ورود و همگام‌سازی"; binding.btnLogin.isEnabled = true
                    Toast.makeText(this@LoginActivity, "خطا: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
