package com.example.apka

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu


class HeaderFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_header, container, false)

        val backBtn = view.findViewById<ImageButton>(R.id.backBtn)
        val menuBtn = view.findViewById<ImageButton>(R.id.menuBtn)

        backBtn.setOnClickListener() {
            activity?.onBackPressed()
        }

        menuBtn.setOnClickListener() {
            val popupMenu = PopupMenu(activity, menuBtn)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                val id = menuItem.itemId

                if (id == R.id.authors) {
                    val intent = Intent(activity, AuthorsActivity::class.java)
                    startActivity(intent)
                } else if (id == R.id.config) {
                    val intent = Intent(activity, CfgActivity::class.java)
                    startActivity(intent)
                }

                true
            }

            popupMenu.show()
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HeaderFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HeaderFragment().apply {

            }
    }
}