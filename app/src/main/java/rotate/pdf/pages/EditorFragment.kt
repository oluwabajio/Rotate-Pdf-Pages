package rotate.pdf.pages

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.VerticalAlignment
import rotate.pdf.pages.adapters.PdfImageAdapter
import rotate.pdf.pages.databinding.FragmentEditorBinding
import java.io.File
import java.io.FileOutputStream
import java.net.MalformedURLException


class EditorFragment : Fragment() {

    lateinit var pdfImages: MutableList<PdfImage>
    private val CREATE_PDF_DOCUMENT: Int = 654
    lateinit var binding: FragmentEditorBinding
    private val tmpFolder: String
        get() = requireContext().filesDir.absolutePath

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditorBinding.inflate(inflater, container, false)

        initAds()
        initInterstitial()

        extractPdfPages()



        return binding.root
    }

    private fun initAds() {

        val adRequest: AdRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }
    private fun initInterstitial() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            requireActivity(),
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError?.toString().toString())
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }
            })
    }

    private fun extractPdfPages() {
        if (arguments?.getString("path") != null) {
            val selectedFilePath = arguments?.getString("path")
            val uri = Uri.parse(selectedFilePath)

            extractPages(uri)
        }
    }

    fun delete(path: String, startsWidth: String = "") {
        File(path).listFiles()?.forEach { file ->
            if (file.isFile) {
                val deleteFile = startsWidth.isEmpty() || file.name.startsWith(startsWidth)
                if (deleteFile) file.delete()
            }
        }
    }

    private fun extractPages(uri: Uri?) {
        val imageList = getImagesFromPDF(uri)

        if (imageList.size > 0) {
            val layoutManager = LinearLayoutManager(requireActivity())
            binding.rvImages.layoutManager = layoutManager

            val adapter = PdfImageAdapter(imageList, requireActivity())
            binding.rvImages.adapter = adapter

            binding.btnSavePdf.visibility = View.VISIBLE
            binding.btnSavePdf.setOnClickListener {
                pdfImages = adapter.getDataList()
                showSaveDialog()

            }
        }
    }

    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Save")
        builder.setMessage("Kindly Choose Folder to save pdf document to")

        // Add the buttons
        builder.setPositiveButton("Choose") { dialogInterface, i ->
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.type = "application/pdf"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.putExtra(Intent.EXTRA_TITLE, "pdf_file.pdf");
            startActivityForResult(
                intent,
                CREATE_PDF_DOCUMENT
            )
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialogInterface, i -> dialogInterface.dismiss() }

        // Create the AlertDialog
        val dialog = builder.show()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            CREATE_PDF_DOCUMENT -> {
                val content = data?.data
                convertImagesToPdf(content!!)
            }
        }
    }


    fun convertImagesToPdf(uri: Uri) {
        var pdfWriter: PdfWriter? =
            PdfWriter(requireActivity().contentResolver.openOutputStream(uri))

        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument, PageSize.A4)
        document.setLeftMargin(0f)
        document.setRightMargin(0f)
        document.setBottomMargin(30f)
        document.setTopMargin(30f)
//        val document = Document(pdfDocument)

        val tmpFolder: String = requireActivity().filesDir.absolutePath
        val tmpImagesFolder = File(tmpFolder, "images")


        for (singleImage in pdfImages) {
            val imagePath = File(tmpImagesFolder, singleImage.image)
            val image: Image = Image(ImageDataFactory.create(imagePath.absolutePath))
            val rotationAngle = when (singleImage.angle) {
                90 -> Math.PI * 0.5
                180 -> Math.PI
                270 -> Math.PI * 1.5
                else -> 0.0 // Default to 0 degrees if angle is not one of the specified values
            }
            image.setRotationAngle(rotationAngle)
            image.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            // Center the image horizontally and vertically on the page
            image.setHorizontalAlignment(HorizontalAlignment.CENTER)

            document.add(image)
        }

        document.close()

        showPdfSaveSuccessfulDialog()

    }

    private fun showPdfSaveSuccessfulDialog() {
        val builder = AlertDialog.Builder(requireActivity())
        with(builder)
        {
            setTitle("Rotate Pdf Pages")
            setMessage("Your pdf has been saved successfully to your selected folder.")
            setPositiveButton("DISMISS", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    showInterstitial()
                    dialog!!.dismiss()
                }
            })
            show()
        }
    }

    private fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(requireActivity())
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

    private fun getImagesFromPDF(uri: Uri?): ArrayList<PdfImage> {

        delete(tmpFolder)
        val tmpImagesFolder = File(tmpFolder, "images")
        tmpImagesFolder.mkdirs()

        val parcelFileDescriptor = requireActivity().contentResolver.openFileDescriptor(uri!!, "r")
        val renderer = PdfRenderer(parcelFileDescriptor!!)

        // Getting total pages count.
        val pageCount = renderer.pageCount

        val arrayList = ArrayList<PdfImage>()
        // Iterating pages
        for (i in 0 until pageCount) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val fileName = "image$i.jpg"
            val file = File(tmpImagesFolder.absolutePath, fileName)

            if (file.exists()) file.delete()

            try {
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                Log.v("Saved Image - ", file.absolutePath)
                out.flush()
                out.close()
                arrayList.add(PdfImage(fileName))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return arrayList

    }


    companion object {
        private const val TAG = "EditorFragment"

    }
}