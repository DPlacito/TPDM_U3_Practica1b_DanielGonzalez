package mx.edu.ittepic.tpdm_u3_practica1b_danielgonzalez

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var descripcion: EditText? = null
    var monto: EditText? = null
    var RSI_Pago: RadioButton? = null
    var insertar: Button? = null
    var listview: ListView? = null
    var layout_fecha : LinearLayout?=null
    var date : TextView?=null

    //Declarando objeto FireStore
    var baseRemota = FirebaseFirestore.getInstance()
    //Declarar objetos tipo Arreglo Dinamico
    var registrosRemotos = ArrayList<String>()
    var keys = ArrayList<String>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(cabezera_Principal)
        title = "Recibo Pagos"

        descripcion = findViewById(R.id.EditText1)
        monto = findViewById(R.id.EditText2)
        layout_fecha = findViewById(R.id.layout_fecha_date)
        RSI_Pago = findViewById(R.id.SiPago)
        insertar = findViewById(R.id.Boton_Insertar)
        listview = findViewById(R.id.Lista)
        date = findViewById(R.id.date_text)

        layout_fecha?.setOnClickListener {
            getDatePickerDialog()
        }

        insertar?.setOnClickListener {
            if(descripcion?.text.toString().isEmpty()||monto?.text.toString().isEmpty()||date?.text.toString().isEmpty()){
                AlertDialog.Builder(this).setTitle("No Puedes Insertar!!!")
                    .setMessage("Llena y/o Completa Todos Los Campos")
                    .setPositiveButton("Ok") { dialogInterface, i ->}
                    .show()
            }else {
                var datosInsertar = hashMapOf(
                    "Descripcion" to descripcion?.text.toString(),
                    "Monto" to monto?.text.toString().toDouble(),
                    "fechaVencimiento" to date?.text.toString(),
                    "Pagado" to RSI_Pago?.isChecked.toString().toBoolean()
                )

                baseRemota.collection("recibopagos")//equivalente a una tabla
                    .add(datosInsertar)
                    // esta esperando que insertes... AS es forzar el tipo de valor
                    .addOnSuccessListener {
                        Toast.makeText(this, "SE INSERTO CORRECTAMENTE", Toast.LENGTH_LONG).show()
                    } //SUccess es el pudo
                    .addOnFailureListener {
                        Toast.makeText(this, "ERROR: NO SE INSERTO", Toast.LENGTH_LONG).show()
                    }//FailureListener Este es el no se puso
                limpiarCampos()
            }
        }

        baseRemota.collection("recibopagos")
            .addSnapshotListener { querySnapshot, e ->
                // Una serie de documentos que es como el cursor que te recupera todos los cambios que se estan realizando en la base de datos
                if (e != null) {
                    Toast.makeText(this, "ERROR NO SE PUEDE HACER LA CONSULTA", Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener   //Si no lo tengo ahi lo dejo
                }
                registrosRemotos.clear() //Antes de que cargue la informacion este este borrado
                keys.clear()
                for (document in querySnapshot!!) {//signos de admiracion le estoy diciendo que estoy seguro que querysnapshot no esta null
                    var cadena = "Descripcion: " + document.getString("Descripcion") + "\n" +
                            "Monto: " + document.getDouble("Monto") + "\n" +
                            "Fecha De Vencimiento: " + document.getString("fechaVencimiento") + "\n"+
                            "Pagado: " + document.getBoolean("Pagado")

                    registrosRemotos.add(cadena)
                    keys.add(document.id)
                }
                if (registrosRemotos.size == 0) {
                    registrosRemotos.add("NO HAY DATOS AUN PARA MOSTRAR")
                }
                var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, registrosRemotos)
                listview?.adapter = adapter
            }
        listview?.setOnItemClickListener { adapterView, view, position, l ->
            if (keys.size == 0) {
                return@setOnItemClickListener
            }
            AlertDialog.Builder(this).setTitle("ATENCION")
                .setMessage("QUE DESEAS HACER CON " + registrosRemotos.get(position) + "?")
                .setPositiveButton("Eliminar") { dialogInterface, i ->
                    baseRemota.collection("recibopagos")
                        .document(keys.get(position))//id de 30 letras o numeros
                        .delete() // NO HAY WHERE POR QUE YA ESTA POSICIONADO EN i
                        .addOnSuccessListener {
                            //Si se logra
                            Toast.makeText(this, "Eiminado", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .addOnFailureListener {
                            // Si no se logra
                            Toast.makeText(this, "No Se Logro Eliminar", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                .setNegativeButton("Actualizar") { dialogInterface, i ->
                    var otroActivity = Intent (this,Main2Activity::class.java)
                    otroActivity.putExtra("id",keys.get(position))
                    startActivity(otroActivity)
                }
                .setNeutralButton("Cancelar") { dialogInterface, i -> }
                .show()
        }
    }

    private fun limpiarCampos() {
        descripcion?.setText("")
        monto?.setText("")
        date?.setText("")
    }

    private fun getDatePickerDialog() {
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)
        var uno = 1

        val dialog = DatePickerDialog(this, { view, year, month, dayOfMonth ->
            var mes = month+uno
            val endDate = "$dayOfMonth.$mes.$year"
            date?.setText(endDate)
        }, year, month , day)

        dialog.datePicker.minDate =
            System.currentTimeMillis()
        dialog.show()
    }

}
