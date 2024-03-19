package rotate.pdf.pages.adapters

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import rotate.pdf.pages.PdfImage
import rotate.pdf.pages.R
import java.io.File

class PdfImageAdapter(private val dataList: MutableList<PdfImage>, private val activity: Activity) :
    RecyclerView.Adapter<PdfImageAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_pdfimage, parent, false)
        return MyViewHolder(view, activity, dataList)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)

    }


    fun getDataList(): MutableList<PdfImage> {
        return dataList
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class MyViewHolder(itemView: View, private val activity: Activity, private val dataList: MutableList<PdfImage>) :
        RecyclerView.ViewHolder(itemView) {
        private val tvAngle: TextView = itemView.findViewById(R.id.tvAngle)
        private val imageView: ImageView = itemView.findViewById(R.id.image)
        private val imgRotate: ImageView = itemView.findViewById(R.id.imgRotate)

        fun bind(pdfImage: PdfImage) {
            val tmpFolder: String = activity.filesDir.absolutePath
            val tmpImagesFolder = File(tmpFolder, "images")
            val image = File(tmpImagesFolder, pdfImage.image)
            val bitmap = BitmapFactory.decodeFile(image.absolutePath)

            val rotatedBitmap = rotateBitmap(bitmap, pdfImage.angle.toFloat())
            imageView.setImageBitmap(rotatedBitmap)


            imgRotate.setOnClickListener {
                var currentAngle = tvAngle.text.toString().toInt()
                currentAngle += 90
                if (currentAngle >= 360) {
                    currentAngle = 0
                }
                tvAngle.text = currentAngle.toString()
//                dataList[adapterPosition].angle = currentAngle

                pdfImage.angle = currentAngle

                // Rotate the bitmap by the new angle
                val rotatedBitmap = rotateBitmap(bitmap, currentAngle.toFloat())
                imageView.setImageBitmap(rotatedBitmap)
            }


        }

        private fun rotateBitmap(bitmap: Bitmap?, angle: Float): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)

        }
    }
}