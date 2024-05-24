package com.capstone.craftopiaproject.creation.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.capstone.craftopiaproject.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CreateFragment : Fragment() {
    private lateinit var productNameEditText: TextInputEditText
    private lateinit var productPriceEditText: TextInputEditText
    private lateinit var productDescriptionEditText: TextInputEditText
    private lateinit var categoryTypeInput: AutoCompleteTextView
    private lateinit var subcategoryInput: AutoCompleteTextView
    private lateinit var uploadButton: Button
    private lateinit var imageLinkTextView: TextView
    private lateinit var createButton: Button
    private lateinit var backButton: ImageButton

    private var selectedImageUri: Uri? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create, container, false)

        productNameEditText = view.findViewById(R.id.product_name)
        productPriceEditText = view.findViewById(R.id.product_price)
        productDescriptionEditText = view.findViewById(R.id.product_description)
        categoryTypeInput = view.findViewById(R.id.categoryType)
        subcategoryInput = view.findViewById(R.id.subCategory)
        uploadButton = view.findViewById(R.id.upload_image_button)
        imageLinkTextView = view.findViewById(R.id.image_create_link)
        createButton = view.findViewById(R.id.create_button)
        backButton = view.findViewById(R.id.Backbutton)

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        setupCategoryTypeDropdown()
        setupSubCategoryDropdown()

        createButton.setOnClickListener {
            createProduct()
        }

        return view
    }

    private fun setupCategoryTypeDropdown() {
        val categories = arrayOf(
            "Textiles and Fabrics",
            "Woodworking",
            "Art & Visual Creations",
            "Stonework"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        categoryTypeInput.setAdapter(adapter)
    }

    private fun setupSubCategoryDropdown() {
        val categories = arrayOf(
            "Creations",
            "Tools",
            "Raw Materials"
        )
        val subcategoryadapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        subcategoryInput.setAdapter(subcategoryadapter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            uploadImageToStorage()
        }
    }

    private fun uploadImageToStorage() {
        val imageRef = storage.reference.child("images/${UUID.randomUUID()}")
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    imageLinkTextView.text = uri.toString()
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createProduct() {
        val productName = productNameEditText.text.toString()
        val productPrice = productPriceEditText.text.toString()
        val productDescription = productDescriptionEditText.text.toString()
        val subCategory = subcategoryInput.text.toString()
        val categoryType = categoryTypeInput.text.toString()
        val imageLink = imageLinkTextView.text.toString()

        if (productName.isEmpty() || productPrice.isEmpty() || productDescription.isEmpty() || categoryType.isEmpty() || imageLink.isEmpty()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price: Double = try {
            productPrice.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Invalid product price", Toast.LENGTH_SHORT).show()
            return
        }

        val product = hashMapOf(
            "name" to productName,
            "price" to price,
            "description" to productDescription,
            "category" to categoryType,
            "subcategory" to subCategory,
            "imageLink" to imageLink,
            "userId" to auth.currentUser?.uid
        )

        db.collection("products")
            .add(product)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(context, "Product created successfully", Toast.LENGTH_SHORT).show()
                val productItem = Product_List(documentReference.id, imageLink, productName, price, categoryType, subCategory)
                Lists.addProduct(productItem)
                activity?.onBackPressed()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to create product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        fun newInstance() = CreateFragment()
    }
}