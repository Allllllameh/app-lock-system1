package com.company.applock
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.company.applock.databinding.ActivityLockBinding
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
class LockScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLockBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) { override fun handleOnBackPressed() {} })
        binding.btnUnlock.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            if (code.length < 4) return@setOnClickListener
            binding.btnUnlock.text = "در حال بررسی..."; binding.btnUnlock.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = Supabase.client.from("access_codes").select { filter { eq("code", code); eq("is_used", false) } }.decodeSingleOrNull<JsonObject>()
                    if (result != null) {
                        val codeId = result["id"]?.jsonPrimitive?.content
                        val duration = result["duration_minutes"]?.jsonPrimitive?.int ?: 0
                        if (codeId != null) {
                            Supabase.client.from("access_codes").update({ set("is_used", true); set("used_at", System.currentTimeMillis()) }) { filter { eq("id", codeId) } }
                            val expiryTime = System.currentTimeMillis() + (duration * 60 * 1000L)
                            getSharedPreferences("AppLockPrefs", MODE_PRIVATE).edit().putLong("unlock_expiry", expiryTime).apply()
                            Toast.makeText(this@LockScreenActivity, "باز شد!", Toast.LENGTH_SHORT).show(); finish()
                        }
                    } else {
                        Toast.makeText(this@LockScreenActivity, "رمز اشتباه است", Toast.LENGTH_LONG).show()
                        binding.btnUnlock.text = "باز کردن"; binding.btnUnlock.isEnabled = true
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LockScreenActivity, "خطای شبکه", Toast.LENGTH_LONG).show()
                    binding.btnUnlock.text = "باز کردن"; binding.btnUnlock.isEnabled = true
                }
            }
        }
    }
}
