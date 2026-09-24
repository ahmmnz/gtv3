package com.googletv.kumanda
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.Socket
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
@Composable
fun App() {
    var vol by remember { mutableStateOf(24) }
    var ch by remember { mutableStateOf(7) }
    var typed by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("192.168.1.100") }
    var log by remember { mutableStateOf("Hazır") }
    val scope = rememberCoroutineScope()
    fun send(c: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val s = Socket(ip, 6466); s.soTimeout=2000; s.getOutputStream().write(c.toByteArray()); s.close()
                withContext(Dispatchers.Main){ log="Gönderildi: $c" }
            } catch(e:Exception){
                withContext(Dispatchers.Main){ log="Bağlanamadı" }
            }
        }
    }
    MaterialTheme {
        Column(Modifier.fillMaxSize().background(Color(0xFF0A0A0F)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("GTV Kumanda v2.1", color=Color.White, fontSize=20.sp, fontWeight=FontWeight.Bold)
            Text("Ses:$vol Kanal:$ch", color=Color(0xFFA78BFA))
            Card(Modifier.fillMaxWidth(), colors=CardDefaults.cardColors(Color(0xFF1A1A2E))) {
                Column(Modifier.padding(12.dp)) {
                    Text("SES", color=Color.White, fontWeight=FontWeight.Bold)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        Button(onClick={ if(vol<100){vol++; send("VOL_UP")} }){Text("Ses +")}
                        Button(onClick={ if(vol>0){vol--; send("VOL_DOWN")} }){Text("Ses -")}
                    }
                    Slider(value=vol.toFloat(), onValueChange={vol=it.toInt()}, valueRange=0f..100f)
                }
            }
            Card(Modifier.fillMaxWidth(), colors=CardDefaults.cardColors(Color(0xFF1A1A2E))) {
                Column(Modifier.padding(12.dp)) {
                    Text("KANAL 0-9", color=Color.White, fontWeight=FontWeight.Bold)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        Button(onClick={ ch++; send("CH_UP") }){Text("Kanal +")}
                        Button(onClick={ if(ch>1){ch--; send("CH_DOWN")} }){Text("Kanal -")}
                    }
                    Text("Yazılan:$typed", color=Color.White)
                    for(r in 0..3){
                        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(4.dp)){
                            for(c in 0..2){
                                val n = if(r==3){ if(c==0)0 else -1 } else r*3+c+1
                                if(n in 0..9){ Button(Modifier.weight(1f), onClick={ if(typed.length<3) typed+=n.toString() }){Text("$n")} }
                                else if(r==3 && c==1){ Button(Modifier.weight(2f), onClick={ typed.toIntOrNull()?.let{ch=it; send("CH_$it"); typed=""} }){Text("Git")} }
                            }
                        }
                    }
                }
            }
            OutlinedTextField(value=ip, onValueChange={ip=it}, label={Text("TV IP")}, modifier=Modifier.fillMaxWidth())
            Text(log, color=Color(0xFF4ADE80), fontSize=12.sp)
        }
    }
}
