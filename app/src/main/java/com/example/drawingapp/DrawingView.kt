package com.example.drawingapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

// Custom View class for drawing
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mDrawPath : CustomPath? = null  // Variable to store the path we want to draw
    private var mCanvasBitmap : Bitmap? = null  // An instance of the bitmap to cache everything drawn
    private var mDrawPaint: Paint? = null  // Paint object that defines drawing properties (like color, stroke width)
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float? = 0.toFloat()  // Size of the brush (stroke width)
    private var color = Color.BLACK  // Current color for drawing
    private var canvas: Canvas? = null  // Canvas object to draw on

    private val mPaths = ArrayList<CustomPath>()  // List to hold all drawn paths (for drawing everything on screen)
    private val mUndoPaths = ArrayList<CustomPath>() // List to hold undone paths (optional feature to support undo)
    private val mRedoPaths = ArrayList<CustomPath>() // List to hold redone paths (optional feature to support redo)

    // Called when this view is first created
    init {
        setUpDrawing() // Setup paints and initial variables
    }

    fun onClickUndo() {
        if(mPaths.size > 0) {
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()  // Redraw the view (calls onDraw)
        }
    }

    fun onClickRedo() {
        if(mUndoPaths.size > 0) {
            mPaths.add(mUndoPaths.removeAt(mUndoPaths.size - 1))
            invalidate()  // Redraw the view (calls onDraw)
        }
    }


    private fun setUpDrawing() {
        mDrawPaint = Paint()  // Create new Paint object
        mDrawPath = CustomPath(color, mBrushSize)  // Create new empty path
        mDrawPaint!!.color = color  // Set default paint color to black
        mDrawPaint!!.style = Paint.Style.STROKE  // This is for stroke style (lines only, no fills)
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND // This is for stroke join, Rounded corner when two lines meet
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND // Rounded edges at the end of the lines
        mCanvasPaint = Paint(Paint.DITHER_FLAG)  // Paint with dithering for smooth colors
        // mBrushSize = 15.toFloat()
    }

    // Called whenever the size of this view changes (like screen rotation)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Create a new bitmap to fit new width and height
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)  // Associate the canvas with this bitmap
    }


    // Actually draw the view's contents on the screen
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the bitmap that holds previous drawings
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        // Draw all saved paths one by one
        for(path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness!!
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        // Draw the current ongoing path (if the user is drawing)
        if(!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness!!
            mDrawPaint!!. color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }


    // Handle touch events (finger on screen)
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Get X and Y coordinates of the touch event
        val touchX = event?.x
        val touchY = event?.y


        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {  // Finger touches the screen
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset() // clear any old drawing in path
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX, touchY) // Move the path to the touched point
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {  // Finger moves on the screen
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY) // draw line to the new point
                    }
                }
            }

            MotionEvent.ACTION_UP -> {  // Finger lifts from screen
                mPaths.add(mDrawPath!!)   // Add the current path to the list of paths
                mDrawPath = CustomPath(color, mBrushSize)  // Prepare the fresh path
            }
            else -> return false
        }

        // Redraw the view (calls onDraw)
        invalidate()
        return true
    }


    // Public function to set a new brush size
    fun setSizeForBrush(newSize: Float) {
        // Convert dp unit into pixels based on device screen density
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            newSize, resources.displayMetrics
        )

        //Update paint stroke width
        mDrawPaint!!.strokeWidth = mBrushSize!!
    }



    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }


    // Custom class to hold a Path along with color and thickness
    internal inner class CustomPath(
        var color: Int,             // Color for this path
        var brushThickness: Float?  // Stroke width for this path
    ) : Path() {    // Inherits from Android's Path class

    }
}