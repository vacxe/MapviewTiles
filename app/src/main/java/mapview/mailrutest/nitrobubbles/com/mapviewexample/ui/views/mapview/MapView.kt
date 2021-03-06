package mapview.mailrutest.nitrobubbles.com.mapviewexample.ui.views.mapview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import mapview.mailrutest.nitrobubbles.com.mapviewexample.R
import java.util.*

/**
 * Created by konstantinaksenov on 08.02.17.
 */

class MapView : FrameLayout, MapViewContract.View {
    val presenter: MapViewPresenter = MapViewPresenter()
    val tiles: HashSet<TileView> = HashSet()//TODO: Hash for reuse tiles -> feature

    val tileSide by lazy {
        context.resources.getDimension(R.dimen.default_tile_weight).toInt()
    }

    companion object Shift {
        var shiftHeight = 0
        var shiftWeight = 0
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : super(context, attrs, defStyleAttr, defStyleRes) {
        addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                if (this@MapView.height != 0 && this@MapView.width != 0) {
                    presenter.viewObtainSizes()
                    removeOnLayoutChangeListener(this)
                }
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        presenter.bindView(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        presenter.unbindView()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                presenter.actionDown(event.x.toInt(), event.y.toInt())
            }
            MotionEvent.ACTION_UP -> {
                presenter.actionUp(event.x.toInt(), event.y.toInt())
            }
            MotionEvent.ACTION_MOVE -> {
                presenter.actionMove(event.x.toInt(), event.y.toInt())
            }
        }
        return true
    }

    override fun shiftTiles(x: Int, y: Int) {
        Shift.shiftHeight -= x
        Shift.shiftWeight -= y
        (0..childCount - 1)
                .map {
                    getChildAt(it) as TileView
                }
                .forEach {
                    if (it.x < -tileSide || it.y < -tileSide || it.y > this.height || it.x > this.width) {
                        removeView(it)
                        tiles.remove(it)
                        presenter.requestUpdate()
                    } else {
                        it.x = it.x + x
                        it.y = it.y + y
                    }
                }

    }

    override fun updateTiles() {
        var tilesByHeight = Math.floor(this.height.toDouble() / tileSide)
        var tilesByWidth = Math.floor(this.width.toDouble() / tileSide)
        var edgeTileIdHeight = Math.floor(Shift.shiftHeight.toDouble() / tileSide).toInt() - 1
        var edgeTileIdWidth = Math.floor(Shift.shiftWeight.toDouble() / tileSide).toInt() - 1


        var shiftByHeight = (Math.abs(Shift.shiftHeight.toDouble()) % tileSide).toInt()
        var shiftByWeight = (Math.abs(Shift.shiftWeight.toDouble()) % tileSide).toInt()

        for (i in -1..tilesByWidth.toInt() + 1)
            for (j in -1..tilesByHeight.toInt() + 1) {
                val tile = TileView(context).setTilePosition(edgeTileIdHeight + i, edgeTileIdWidth + j)
                if (Shift.shiftHeight > 0) {
                    tile.x = (tileSide * i).toFloat() - shiftByHeight
                } else {
                    tile.x = (tileSide * i).toFloat() + shiftByHeight
                }

                if (Shift.shiftWeight > 0) {
                    tile.y = (tileSide * j).toFloat() - shiftByWeight
                } else {
                    tile.y = (tileSide * j).toFloat() + shiftByWeight
                }
                tiles.add(tile)
                addView(tile)
            }
    }
}
