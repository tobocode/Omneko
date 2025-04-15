package dev.tobo.omneko.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import dev.tobo.omneko.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val _updating = MutableStateFlow(false)
    val updating: StateFlow<Boolean> = _updating.asStateFlow()

    fun updateYoutubeDL(context: Context) {
        if (_updating.value) return

        val toast = Toast.makeText(context, context.getString(R.string.updating_youtubedl_toast), Toast.LENGTH_SHORT)
        toast.show()

        viewModelScope.launch(Dispatchers.IO) {
            _updating.value = true

            try {
                YoutubeDL.getInstance().init(context)
                YoutubeDL.getInstance().updateYoutubeDL(context)

                withContext(Dispatchers.Main) {
                    val toast = Toast.makeText(context, context.getString(R.string.youtubedl_update_complete_toast), Toast.LENGTH_SHORT)
                    toast.show()
                }

                _updating.value = false
            } catch (e: YoutubeDLException) {
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    val toast = Toast.makeText(context, context.getString(R.string.youtubedl_update_failed_toast), Toast.LENGTH_SHORT)
                    toast.show()
                }

                _updating.value = false
            }
        }
    }
}