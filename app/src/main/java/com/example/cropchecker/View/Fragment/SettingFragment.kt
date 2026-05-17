package com.example.cropchecker

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class SettingsFragment : Fragment() {

    private lateinit var languageSettings: LinearLayout
    private lateinit var notificationSettings: LinearLayout
    private lateinit var contactSupport: LinearLayout
    private lateinit var aboutApp: LinearLayout
    private lateinit var logoutButton: LinearLayout
    private lateinit var followMeLayout: LinearLayout
    private lateinit var instagramIcon: ImageView
    private lateinit var linkedinIcon: ImageView
    private lateinit var githubIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        languageSettings = view.findViewById(R.id.languageSettings)
        notificationSettings = view.findViewById(R.id.notificationSettings)
        contactSupport = view.findViewById(R.id.contactSupport)
        aboutApp = view.findViewById(R.id.aboutApp)
        logoutButton = view.findViewById(R.id.logoutButton)

        followMeLayout = view.findViewById(R.id.followMeLayout)
        instagramIcon = view.findViewById(R.id.instagramIcon)
        linkedinIcon = view.findViewById(R.id.linkedinIcon)
        githubIcon = view.findViewById(R.id.githubIcon)

        // Language settings
        languageSettings.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // Notification Settings
        notificationSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            }
            startActivity(intent)
        }

        // Contact Support
        contactSupport.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("adilrazakhan158@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Support Request - Plant Leaf Checker App")
            }
            startActivity(Intent.createChooser(emailIntent, "Send Email"))
        }

        // About App
        aboutApp.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("About CropChecker")
                .setMessage("Plant Leaf Checker is an AI-powered application designed to help you monitor and assess the health of your crops with ease.\n\nVersion 1.1\nDeveloped by Adil")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            dialog.setOnShowListener {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                okButton.setTextColor(resources.getColor(android.R.color.black, null))
            }

            dialog.show()
        }

        // Follow Me Links
        instagramIcon.setOnClickListener {
            openLink("https://www.instagram.com/adil.khan_._/")
        }

        linkedinIcon.setOnClickListener {
            openLink("https://www.linkedin.com/in/adil-khan-ba65b7229/")
        }

        githubIcon.setOnClickListener {
            openLink("https://github.com/Adilkhan656")
        }

        // Logout
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), SignIn::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "हिंदी", "தமிழ்", "اردو", "ગુજરાતી")
        val languageCodes = arrayOf("en", "hi", "ta", "ur", "gu")

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Language")
            .setItems(languages) { _, which ->
                setLocale(languageCodes[which])
            }
            .create()
            .show()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        requireActivity().recreate()
    }
}
