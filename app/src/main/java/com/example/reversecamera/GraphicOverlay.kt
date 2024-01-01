package com.example.reversecamera

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.example.reversecamera.utils.Constants.lengthOfCar
import com.example.reversecamera.utils.Constants.widthOfCar
import java.lang.StrictMath.abs
import kotlin.math.sqrt
import kotlin.properties.Delegates
import kotlin.reflect.KProperty


class GraphicOverlay : View {
    var paint = Paint()
    var redPaint = Paint()
    var degree : Int = 0
    var degree1: Int = 0
    var degree2: Int = 0
    var degree3: Int = 0
    var degree4: Int = 0
    var leftdegree : Int = 150
    lateinit var seekbar: SeekBar
    var new_width =0
    var new_height =0
    object varib{
        var newval : MutableLiveData<Int>? = MutableLiveData()
        var newval1 : MutableLiveData<Int>? = MutableLiveData()
        var newval2 : MutableLiveData<Int>? = MutableLiveData()
        var newval3 : MutableLiveData<Int>? = MutableLiveData()
        var newval4 : MutableLiveData<Int>? = MutableLiveData()
        var degree : Int by Delegates.observable(0) { kProperty: KProperty<*>, old: Int, new: Int ->
            newval?.value = new
        }

        var degree1 : Int by Delegates.observable(0) { kProperty: KProperty<*>, old: Int, new: Int ->
            newval1?.value = new
        }
        var degree2 : Int by Delegates.observable(0) { kProperty: KProperty<*>, old: Int, new: Int ->
            newval2?.value = new
        }
        var degree3 : Int by Delegates.observable(0) { kProperty: KProperty<*>, old: Int, new: Int ->
            newval3?.value = new
        }
        var degree4 : Int by Delegates.observable(0) { kProperty: KProperty<*>, old: Int, new: Int ->
            newval3?.value = new
        }
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        paint.apply {
            color = Color.GREEN
            strokeWidth = 20f
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            pathEffect = CornerPathEffect(10F)
            isAntiAlias = true
        }
        redPaint.color = Color.RED
        redPaint.strokeWidth = 20f

    }
    fun setupDegree(newDegree : Int){
       degree = newDegree
//       invalidate()
       Log.v("setupdegree","${degree}")

   }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.v("onDraw","onDraw called")


        varib.newval?.observe(context as LifecycleOwner){
            degree = (it*40)/756
            leftdegree = -it
        }
        varib.newval1?.observe(context as LifecycleOwner){
            // for width
            degree1 = it

        }
        varib.newval2?.observe(context as LifecycleOwner){
           // for trapezium
            degree2 = it

        }
        varib.newval3?.observe(context as LifecycleOwner){
            // for trapezium
            degree3 = it

        }
        varib.newval4?.observe(context as LifecycleOwner){
            // for trapezium
            degree4 = it

        }
        setupStaticLines(canvas)

    }


    private fun setupStaticLines(canvas: Canvas){
        var height =0
        var width = 0

        height = new_height
        width = new_width


        Log.v("height,width","${height},${width}")

        var left_point = (width - widthOfCar)/2
        var left_mid_point =(width - widthOfCar)/4
        var right_point = (left_point + widthOfCar).toFloat()
        var right_mid_point = right_point + left_mid_point

        val matrix = Matrix()
        val src = floatArrayOf(left_point.toFloat(), (height - lengthOfCar).toFloat(),
            right_point, (height - lengthOfCar).toFloat(),
            right_point.toFloat(), height.toFloat(),
            left_point.toFloat(), height.toFloat())

        val dst = floatArrayOf(left_point.toFloat() + degree1,
            (height - lengthOfCar).toFloat(),
            right_point - degree1, (height - lengthOfCar).toFloat(),

            right_point + degree2, height.toFloat(),
            left_point.toFloat() - degree2 , height.toFloat())


        matrix.setPolyToPoly(src, 0, dst, 0, 4)

        canvas.concat(matrix)
        canvas.apply {
// upper static red line
            drawLine(left_point.toFloat(),
                (height - lengthOfCar).toFloat(), right_point.toFloat(), (height - lengthOfCar).toFloat(), redPaint)
            drawCircle(left_point.toFloat(), (height - lengthOfCar).toFloat(), 10f, redPaint)
            drawCircle(right_point.toFloat(), (height - lengthOfCar).toFloat(), 10f, redPaint)

            drawLine(left_point.toFloat(),(height - lengthOfCar).toFloat(), left_point.toFloat(),height.toFloat() , redPaint)

            drawLine( right_point.toFloat(), (height - lengthOfCar).toFloat(),right_point.toFloat(), (height).toFloat(), redPaint)
            var path = Path()
            Log.v("vishalnewdegree","${degree}")
            path.moveTo(left_point.toFloat(), height.toFloat())

            var radiusOfInnerCircle = 0f
            var radiusOfOuterCircle = 0f
            var leftCenter = 0f
            var rightCenter = 0f
            var radiusOfGeneralCircle1 = 0f
            var radiusOfGeneralCircle2= 0f

            if(degree <0){


                val radiusOfGeneralCircle = 1*(lengthOfCar + widthOfCar*Math.tan(Math.toRadians(-1.0*degree))/2)/Math.tan(Math.toRadians(-1.0*degree))
                Log.v("+radiusOfGeneralCircle","${radiusOfGeneralCircle}")

                val radiusOfInnerCircle = radiusOfGeneralCircle - widthOfCar/2
                leftCenter = (left_point - radiusOfInnerCircle).toFloat()
                radiusOfOuterCircle = (radiusOfGeneralCircle + widthOfCar/2).toFloat()
                var angle = ((lengthOfCar)/(radiusOfInnerCircle))*180/Math.PI
                path = Path()
                path.moveTo(right_point.toFloat(), height.toFloat())
                val x2_ = leftCenter + radiusOfOuterCircle*Math.cos(Math.toRadians(angle))
                val y2_ = height - radiusOfOuterCircle*Math.sin(Math.toRadians(angle))
                val controlx2_ = leftCenter + radiusOfOuterCircle*Math.cos(Math.toRadians(angle/2))
                val controly2_ = height - radiusOfOuterCircle*Math.sin(Math.toRadians(angle/2))

               Log.v("-ve degree","${radiusOfOuterCircle}")


                path.quadTo(controlx2_.toFloat(),
                    controly2_.toFloat(),
                    x2_.toFloat(),
                    y2_.toFloat())
                canvas.drawPath(path,paint)
                path.reset()

                // drawing left line
                path.moveTo(left_point.toFloat(), height.toFloat())

                Log.v("angle","${angle}")
//                val x2 = h + radiusOfInnerCircle*Math.cos(Math.toRadians(angle))
                val x2 = leftCenter + radiusOfInnerCircle*Math.cos(Math.toRadians(angle))
                val y2 = height - radiusOfInnerCircle*Math.sin(Math.toRadians(angle))
//                val controlx2 = h + radiusOfInnerCircle*Math.cos(Math.toRadians(angle/2))
                val controlx2 = leftCenter + radiusOfInnerCircle*Math.cos(Math.toRadians(angle/2))
                val controly2 = height - radiusOfInnerCircle*Math.sin(Math.toRadians(angle/2))

                path.quadTo(controlx2.toFloat(),
                    controly2.toFloat(),
                    x2.toFloat(),
                    y2.toFloat())
                canvas.drawPath(path,paint)
                path.reset()
                drawLine(
                    x2_.toFloat(),
                    y2_.toFloat(), x2.toFloat(), y2.toFloat(), paint)

            }
            else{



                 val radiusOfGeneralCircle = 1*(lengthOfCar + widthOfCar*Math.tan(Math.toRadians(1.0*degree))/2)/Math.tan(Math.toRadians(1.0*degree))
                     Log.v("-radiusOfGeneralCircle","${radiusOfGeneralCircle}")



//                val radiusOfInnerCircle = Math.sqrt(
//                    Math.pow(lengthOfCar.toDouble(), 2.0) + Math.pow(radiusOfGeneralCircle+widthOfCar/2,2.0)
//                )
//                val radiusOfInnerCircle = radiusOfGeneralCircle + widthOfCar/2
                  radiusOfOuterCircle = (radiusOfGeneralCircle + widthOfCar/2).toFloat()
                  radiusOfInnerCircle = (radiusOfGeneralCircle - widthOfCar/2).toFloat()
                 rightCenter = (left_point + radiusOfOuterCircle).toFloat()
              //  right_point = rightCenter - radiusOfInnerCircle
                Log.v("+vedegree","${radiusOfOuterCircle}")

//     drawing left outer circle
                path = Path()
                path.moveTo(left_point.toFloat(), height.toFloat())

//                var angle = ((3*height/4)/(radiusOfOuterCircle))*180/Math.PI
//                var angle = ((lengthOfCar)/(radiusOfOuterCircle))*180/Math.PI
                var angle = ((lengthOfCar)/(radiusOfInnerCircle))*180/Math.PI
                val x2_ = rightCenter -  radiusOfOuterCircle*Math.cos(Math.toRadians(angle))
                val y2_ = height - radiusOfOuterCircle*Math.sin(Math.toRadians(angle))
                val controlx2_ = rightCenter - radiusOfOuterCircle*Math.cos(Math.toRadians(angle/2))
                val controly2_ = height - radiusOfOuterCircle*Math.sin(Math.toRadians(angle/2))

                path.quadTo(controlx2_.toFloat(),
                    controly2_.toFloat(),
                    x2_.toFloat(),
                    y2_.toFloat())




                canvas.drawPath(path,paint)
                // drawing inner right circle
                path.reset()
                path.moveTo(right_point.toFloat(),height.toFloat())
                val x2 = rightCenter -  radiusOfInnerCircle*Math.cos(Math.toRadians(angle))
                val y2 = height - radiusOfInnerCircle*Math.sin(Math.toRadians(angle))
                val controlx2 = rightCenter - radiusOfInnerCircle*Math.cos(Math.toRadians(angle/2))
                val controly2 = height - radiusOfInnerCircle*Math.sin(Math.toRadians(angle/2))


                path.quadTo(controlx2.toFloat(),
                    controly2.toFloat(),
                    x2.toFloat(),
                    y2.toFloat())



                canvas.drawPath(path,paint)
                path.reset()
                drawLine(
                    x2_.toFloat(),
                    y2_.toFloat(), x2.toFloat(), y2.toFloat(), paint)
            }
            invalidate()
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        new_width = w
        new_height = h
    }


}