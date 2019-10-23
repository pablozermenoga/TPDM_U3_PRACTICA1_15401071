package mx.edu.ittepic.tpdm_u3_practica1_15401071

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    var descripcion :  EditText?=null
    var monto : EditText?=null
    var fechaVencimiento : EditText?=null
    var pagado : EditText?=null
    var insertar : Button?=null
    var listView : ListView?=null
    var actualizar: Button?=null
    //-----------------------------------------
    var baseRemota= FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        descripcion=findViewById(R.id.descripcion)
        monto=findViewById(R.id.monto)
        fechaVencimiento=findViewById(R.id.fecha)
        pagado=findViewById(R.id.pagado)
        insertar=findViewById(R.id.insertar)
        listView=findViewById(R.id.listview)
        actualizar=findViewById(R.id.actualizar)
        var registrosRemotos = ArrayList<String>()
        var keys = ArrayList<String>()
        var id=""

        insertar?.setOnClickListener {
            var datosInsertar = hashMapOf(
                "descripcion" to descripcion?.text.toString(),
                "monto" to monto?.text.toString().toDouble(),
                "fechaVencimiento" to fechaVencimiento?.text.toString(),
                "pagado" to pagado?.text.toString()
            )

            baseRemota.collection("Recibopagos").add(datosInsertar as Map<String,Any>)
                .addOnSuccessListener {
                    Toast.makeText(this,"SE INSERTÓ CORRECTAMENTE :D",Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this,"NO SE INSERTÓ D:",Toast.LENGTH_LONG).show()
                }
                limpiarC()
        }
        baseRemota.collection("Recibopagos").addSnapshotListener { querySnapshot, e ->
            if (e!=null){
                Toast.makeText(this,"ERROR NO SE PUDE HACER LA CONSULTA",Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            registrosRemotos.clear()
            keys.clear()

            for (document in querySnapshot!!){ //signos de admiracion significan que una variable querysnapshot no esta en null

                var cad = document.getString("descripcion")+"--"+document.getString("fechaVencimiento")+"--"+document.getDouble("monto")+"--"+document.getString("pagado")+"\n"
                //GETSTRING PARA OBTENENER UNA CADENA

                registrosRemotos.add(cad)
                keys.add(document.id)
            }
            if (registrosRemotos.size==0){
                registrosRemotos.add("NO HAY DATOS AUN PARA MOSTRAR")
            }
            var adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,registrosRemotos)
            listView?.adapter=adapter
        }

        listView?.setOnItemClickListener { adapterView, view, i, l ->
            if (keys.size==0){
                return@setOnItemClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("ATENCION USUARIO")
                .setMessage("¿QUE ACCION DESEA REALIZAR CON EL REGISTRO"+registrosRemotos.get(i)+"?")
                .setPositiveButton("ELIMINAR"){dialog, which ->
                    baseRemota.collection("Recibopagos").document(keys.get(i))
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this,"REGISTRO BORRADO",Toast.LENGTH_LONG).show()

                        }
                        .addOnFailureListener {
                            Toast.makeText(this,"NO ES POSIBLE ELIMINAR",Toast.LENGTH_LONG).show()

                        }
                }.setNegativeButton("ACTUALIZAR"){dialog,which->
                    baseRemota.collection("Recibopagos").document(keys.get(i)).get()
                        .addOnSuccessListener {
                            descripcion?.setText(it.getString("descripcion"))
                            fechaVencimiento?.setText(it.getString("fechaVencimiento"))
                            monto?.setText(it.getDouble("monto").toString())
                            pagado?.setText(it.getString("pagado"))
                        }
                        .addOnFailureListener {
                            Toast.makeText(this,"ERROR",Toast.LENGTH_LONG).show()
                        }
                    actualizar?.setOnClickListener {
                        var datosActualizar = hashMapOf(
                            "descripcion" to descripcion?.text.toString(),
                            "fechaVencimiento" to fechaVencimiento?.text.toString(),
                            "monto" to monto?.text.toString().toDouble(),
                            "pagado" to  pagado?.text.toString()
                        )
                        baseRemota.collection("Recibopagos").document(keys.get(i)).set(datosActualizar as Map<String,Any>)
                            .addOnSuccessListener {
                                limpiarC()
                                Toast.makeText(this,"se actualizó", Toast.LENGTH_LONG)
                                    .show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this,"no se actualizó ",Toast.LENGTH_LONG)
                                    .show()
                                descripcion?.isEnabled=false
                                fechaVencimiento?.isEnabled=false
                                pagado?.isEnabled=false
                            }
                    }
                }.setNeutralButton("CANCELAR"){dialog,which->

                }.show()
        }

    }
    fun limpiarC(){
        descripcion?.setText("")
        monto?.setText("")
        fechaVencimiento?.setText("")
        pagado?.setText("")
    }
}
