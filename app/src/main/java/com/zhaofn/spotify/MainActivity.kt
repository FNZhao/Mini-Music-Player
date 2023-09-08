package com.zhaofn.spotify

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import coil.compose.AsyncImage
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zhaofn.spotify.datamodel.Section
import com.zhaofn.spotify.network.NetworkApi
import com.zhaofn.spotify.network.NetworkModule
import com.zhaofn.spotify.player.PlayerBar
import com.zhaofn.spotify.player.PlayerViewModel
import com.zhaofn.spotify.ui.theme.SpotifyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Retrofit
import javax.inject.Inject

// customized extend AppCompatActivity
@AndroidEntryPoint//每个entrypoint是injection的入口
class MainActivity : AppCompatActivity() {

    @Inject//让hilt帮忙inject Retrofit，它帮我们做的其实就是call了一个set方法，但是不知道是怎么new出来的，所以我们要告诉hilt
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var databaseDao: DatabaseDao

    private val playerViewModel: PlayerViewModel by viewModels()//这里不可能出现by fragmentViewModel， 因为安卓不会要求activity一定要有fragment，并且fragment也不能保证在activity之前存活

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)//填充了页面内容

        // find the bottom nav view
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        // get navController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.setGraph(R.navigation.nav_graph)

        NavigationUI.setupWithNavController(navView, navController)

        // https://stackoverflow.com/questions/70703505/navigationui-not-working-correctly-with-bottom-navigation-view-implementation
//        navView.setOnItemSelectedListener{
//            NavigationUI.onNavDestinationSelected(it, navController)
//            navController.popBackStack(it.itemId, inclusive = false)
//            true
//        }

        //这里是把compose写法和xml写法结合
        val playerBar = findViewById<ComposeView>(R.id.player_bar)
        playerBar.apply {
            setContent {
                MaterialTheme(colors = darkColors()) {
                    PlayerBar(viewModel = playerViewModel)
                }
            }
        }

        GlobalScope.launch (Dispatchers.IO) { //开一个IO thread//通过retrofit api拿数据//当launch里面有Dispatchers的时候才会开新的thread，并且开启新的thread也会消耗时间
            val retrofit = NetworkModule.provideRetrofit()
            val api = retrofit.create(NetworkApi::class.java)
            val call: Call<List<Section>> = api.getHomeFeed()
            val feed: List<Section>? = call.execute().body()
            Log.d("Network", feed.toString())
        }

        //View -> UI
        //ViewModel -> connect view and repository, hold state and update state
        //Model (Data Model) -> fetch data

        GlobalScope.launch {//现在有个机制会阻止这部分代码放在main thread里
            withContext(Dispatchers.IO) {
//                val album = Album(
//                    id = 1,
//                    name =  "Hexagonal",
//                    year = "2008",
//                    cover = "https://upload.wikimedia.org/wikipedia/en/6/6d/Leessang-Hexagonal_%28cover%29.jpg",
//                    artists = "Lesssang",
//                    description = "Leessang (Korean: 리쌍) was a South Korean hip hop duo, composed of Kang Hee-gun (Gary or Garie) and Gil Seong-joon (Gil)"
//                )
//                databaseDao.favoriteAlbum(album)
            }
        }
    }
}
//只要remember的值发生改变，则触发recompose，类似与react的rerender
//我们尽量会让component变成stateless，以方便复用与测试//如果把state放在里面我们需要每次都用function去更改，如果function定义在里面则这个component的逻辑就定死了

@Composable
fun LoadSection(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body2,
            color = Color.White
        )
    }
}

@Composable
fun AlbumCover() {
    Column {
        Box(modifier = Modifier.size(160.dp)) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                model = "https://upload.wikimedia.org/wikipedia/en/d/d1/Stillfantasy.jpg",
                contentDescription = null
            )

            Text(
                text = "Still Fantasy",
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 4.dp, start = 4.dp)
                    .align(Alignment.BottomStart)
            )
        }

        Text(
            text = "Jay Chou",
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
            color = Color.LightGray,
        )
    }
}

@Preview
@Composable
fun PreviewAlbumCover() {
    SpotifyTheme {
        Surface {
            AlbumCover()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SpotifyTheme {
        Surface {
            LoadSection("Screen is loading")
        }
    }
}

