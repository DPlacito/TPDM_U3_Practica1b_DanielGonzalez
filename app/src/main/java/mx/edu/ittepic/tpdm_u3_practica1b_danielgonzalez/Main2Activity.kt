package mx.edu.ittepic.tpdm_u3_practica1b_danielgonzalez

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main2.*
import java.util.*

class Main2Activity : AppCompatActivity() {

    private var descripcion: EditText? = null
    private var monto: EditText? = null
    private var RSI_Pago: RadioButton? = null
    private var actualizar: Button? = null
    private var layout_fecha: LinearLayout? = null
    private var date: TextView? = null
    private var id = ""
    var baseRemota = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(cabezera_Principal_Actualizar)
        title = "Actualizar"
        //Aqui me regreso al activity anterior
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        descripcion = findViewById(R.id.Actualizar_EditText1)
        monto = findViewById(R.id.Actualizar_EditText2)
        layout_fecha = findViewById(R.id.Actualizar_layout_fecha_date)
        RSI_Pago = findViewById(R.id.Actualizar_SiPago)
        actualizar = findViewById(R.id.Boton_Actualizar)
        date = findViewById(R.id.Actualizar_date_text)

        id = intent.extras?.getString("id").toString()!!

        baseRemota.collection("recibopagos")
            .document(id)
            .get()
            .addOnSuccessListener {
                descripcion?.setText(it.getString("Descripcion"))
                monto?.setText(it.getDouble("Monto").toString())
                date?.setText(it.getString("fechaVencimiento"))
                RSI_Pago?.isChecked = it.getBoolean("Pagado")!!
            }
        layout_fecha?.setOnClickListener {
            getDatePickerDialog()
        }

        actualizar?.setOnClickListener {
            var datosActualizar = hashMapOf(
                "Descripcion" to descripcion?.text.toString(),
                "Monto" to monto?.text.toString().toDouble(),
                "fechaVencimiento" to date?.text.toString(),
                "Pagado" to RSI_Pago?.isChecked.toString().toBoolean()
            )
            baseRemota.collection("recibopagos")
                .document(id)
                .set(datosActualizar)
                .addOnSuccessListener {
                    Toast.makeText(this, "Se Actualizo Correctamente", Toast.LENGTH_SHORT).show()
                    limpiarCampos()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "No Se Pudo Actualizar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun limpiarCampos() {
        descripcion?.setText("")
        monto?.setText("")
        date?.setText("Toque Aqui")
    }

    private fun getDatePickerDialog() {
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)
        var uno = 1

        val dialog = DatePickerDialog(this, { view, year, month, dayOfMonth ->
            var mes = month+uno
            var endDate = "$dayOfMonth.$mes .$year"
            date?.setText(endDate)
        }, year, month, day)

        dialog.datePicker.minDate =
            System.currentTimeMillis()
        dialog.show()
    }

    //Regresarme Pa' Atras
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else
            super.onOptionsItemSelected(item)
    }

}
