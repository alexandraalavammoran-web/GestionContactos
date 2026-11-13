//Este es el "cerebro" de nuestra aplicación. Conecta la Vista con el Modelo: capta las acciones que hace el usuario
//Aplica las reglas de negocio, actualiza la tabla en tiempo real, controla la animación de la barra de progreso y calcula las estadísticas.
package Controller;

import Model.Persona;
import Model.PersonaDAO;
import View.VentanaContactos;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JSpinner;

 
 //MEJORAS DE CLASE 5: Controlador optimizado bajo el Patrón MVC.
 //Se añadio eventos avanzados, filtrado en tiempo real, exportación CSV.
 //animación de barra de progreso y manejo seguro de modificación/eliminación.
public class logicaContactos implements ActionListener {
    private VentanaContactos delegado;
    private TableRowSorter<DefaultTableModel> sorter;
    private int filaSeleccionada = -1;

    public logicaContactos(VentanaContactos delegado){
        this.delegado = delegado;
        
        // 1. Asignación de eventos a los botones de la interfaz
        if (this.delegado.btn_agregar != null) this.delegado.btn_agregar.addActionListener(this);
        if (this.delegado.btn_modificar != null) this.delegado.btn_modificar.addActionListener(this);
        if (this.delegado.btn_eliminar != null) this.delegado.btn_eliminar.addActionListener(this);
        if (this.delegado.btn_exportar != null) this.delegado.btn_exportar.addActionListener(this);

        // 2. Evento de ratón en la JTable para selección y carga automática de datos
        this.delegado.tbl_contactos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarContactoSeleccionado();
            }
        });

        configurarBuscador();
        actualizarEstadisticas();
    }

    public void cargarContactoSeleccionado() {
        int filaVis = delegado.tbl_contactos.getSelectedRow();
        if (filaVis != -1) {
            int filaReal = delegado.tbl_contactos.convertRowIndexToModel(filaVis);
            DefaultTableModel modelo = (DefaultTableModel) delegado.tbl_contactos.getModel();
            
            // Cargar los valores de la tabla de regreso a los campos del formulario
            delegado.txt_nombre.setText(modelo.getValueAt(filaReal, 0) != null ? modelo.getValueAt(filaReal, 0).toString() : "");
            
            try {
                Object valEdad = modelo.getValueAt(filaReal, 1);
                if (valEdad != null) {
                    delegado.spinner_Edad.setValue(Integer.parseInt(valEdad.toString()));
                }
            } catch (Exception ignored) {}

            delegado.txt_telefono.setText(modelo.getValueAt(filaReal, 2) != null ? modelo.getValueAt(filaReal, 2).toString() : "");
            delegado.txt_cedula.setText(modelo.getValueAt(filaReal, 3) != null ? modelo.getValueAt(filaReal, 3).toString() : "");
            delegado.txt_email.setText(modelo.getValueAt(filaReal, 4) != null ? modelo.getValueAt(filaReal, 4).toString() : "");
            
            if (modelo.getValueAt(filaReal, 5) != null) {
                delegado.cmb_pais.setSelectedItem(modelo.getValueAt(filaReal, 5).toString());
            }
            if (modelo.getValueAt(filaReal, 6) != null) {
                delegado.cmb_categoria.setSelectedItem(modelo.getValueAt(filaReal, 6).toString());
            }
            
            if (modelo.getValueAt(filaReal, 7) != null) {
                String fav = modelo.getValueAt(filaReal, 7).toString();
                delegado.chb_favorito.setSelected(fav.equalsIgnoreCase("Sí"));
            }
        }
    }

    private void configurarBuscador() {
        DefaultTableModel modelo = (DefaultTableModel) delegado.tbl_contactos.getModel();
        sorter = new TableRowSorter<>(modelo);
        delegado.tbl_contactos.setRowSorter(sorter);

        if (delegado.txt_buscar != null) {
            delegado.txt_buscar.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    aplicarFiltro();
                }
            });
        }
    }

    private void aplicarFiltro() {
        String texto = delegado.txt_buscar.getText();
        if (texto.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 0));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e){
        if (delegado.btn_agregar != null && e.getSource() == delegado.btn_agregar){
            agregarContacto();
        } 
        else if (delegado.btn_modificar != null && e.getSource() == delegado.btn_modificar){
            guardarModificacionContacto();
        }
        else if (delegado.btn_eliminar != null && e.getSource() == delegado.btn_eliminar){
            eliminarContacto();
        }
        else if (delegado.btn_exportar != null && e.getSource() == delegado.btn_exportar){
            exportarCSV();
        }
    }

    private void agregarContacto() {
        String nombres = delegado.txt_nombre.getText().trim();
        String edad = delegado.spinner_Edad.getValue().toString(); 
        String telefono = delegado.txt_telefono.getText().trim();
        String cedula = delegado.txt_cedula.getText().trim();
        String email = delegado.txt_email.getText().trim();
        String pais = delegado.cmb_pais.getSelectedItem().toString();
        String categoria = delegado.cmb_categoria.getSelectedItem().toString();
        boolean favorito = delegado.chb_favorito.isSelected();

        if (!nombres.isEmpty() && !telefono.isEmpty() && !cedula.isEmpty() && !email.isEmpty()){
            if (!categoria.equalsIgnoreCase("Elija una categoría") && !categoria.equalsIgnoreCase("Item 1") && !categoria.equalsIgnoreCase("(defaut)") ){

                try {
                    Persona persona = new Persona(nombres, edad, telefono, cedula, email, pais, categoria, favorito);
                    PersonaDAO dao = new PersonaDAO(persona);
                    dao.escribirArchivo();

                    DefaultTableModel modeloTabla = (DefaultTableModel) delegado.tbl_contactos.getModel();
                    String esFavoritoTexto = favorito ? "Sí" : "No";
                    
                    modeloTabla.addRow(new Object[]{nombres, edad, telefono, cedula, email, pais, categoria, esFavoritoTexto});

                    animarBarraProgreso();
                    actualizarEstadisticas();
                    JOptionPane.showMessageDialog(delegado, "¡Contacto Registrado con Éxito!");
                    limpiarCampos();

                } catch (IOException ex){
                    JOptionPane.showMessageDialog(delegado, "Error al guardar en el archivo.");
                }
            } else {
                JOptionPane.showMessageDialog(delegado, "Por favor, elija una categoría válida.");
            }
        } else {
            JOptionPane.showMessageDialog(delegado, "Todos los campos de texto deben estar llenos.");
        }
    }

    public void guardarModificacionContacto() {
        int filaVis = delegado.tbl_contactos.getSelectedRow();
        if (filaVis == -1) {
            JOptionPane.showMessageDialog(delegado, "Seleccione un contacto de la tabla para modificar.");
            return;
        }
        
        int filaReal = delegado.tbl_contactos.convertRowIndexToModel(filaVis);
        DefaultTableModel modelo = (DefaultTableModel) delegado.tbl_contactos.getModel();
        
        String nombre = delegado.txt_nombre.getText();
        int edad = (Integer) delegado.spinner_Edad.getValue();
        String telefono = delegado.txt_telefono.getText();
        String cedula = delegado.txt_cedula.getText();
        String email = delegado.txt_email.getText();
        String pais = delegado.cmb_pais.getSelectedItem().toString();
        String categoria = delegado.cmb_categoria.getSelectedItem().toString();
        boolean favorito = delegado.chb_favorito.isSelected();
        
        modelo.setValueAt(nombre, filaReal, 0);
        modelo.setValueAt(edad, filaReal, 1);
        modelo.setValueAt(telefono, filaReal, 2);
        modelo.setValueAt(cedula, filaReal, 3);
        modelo.setValueAt(email, filaReal, 4);
        modelo.setValueAt(pais, filaReal, 5);
        modelo.setValueAt(categoria, filaReal, 6);
        modelo.setValueAt(favorito ? "Sí" : "No", filaReal, 7);
        
        animarBarraProgreso();
        actualizarEstadisticas();
        JOptionPane.showMessageDialog(delegado, "Contacto modificado con éxito.");
        limpiarCampos();
    }

    private void eliminarContacto() {
        int filaVis = delegado.tbl_contactos.getSelectedRow();
        if (filaVis < 0) {
            JOptionPane.showMessageDialog(delegado, "Seleccione el contacto de la tabla que desea eliminar.");
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(delegado, "¿Está seguro de eliminar este contacto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            int filaReal = delegado.tbl_contactos.convertRowIndexToModel(filaVis);
            DefaultTableModel modelo = (DefaultTableModel) delegado.tbl_contactos.getModel();
            modelo.removeRow(filaReal);

            animarBarraProgreso();
            actualizarEstadisticas();
            JOptionPane.showMessageDialog(delegado, "Contacto eliminado correctamente.");
            limpiarCampos();
        }
    }

    private void exportarCSV() {
        DefaultTableModel modelo = (DefaultTableModel) delegado.tbl_contactos.getModel();
        if (modelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(delegado, "No hay contactos para exportar.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo CSV");
        int userSelection = fileChooser.showSaveDialog(delegado);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File archivoToSave = fileChooser.getSelectedFile();
            String ruta = archivoToSave.getAbsolutePath();
            if (!ruta.endsWith(".csv")) {
                ruta += ".csv";
            }

            try (FileWriter csv = new FileWriter(ruta)) {
                for (int i = 0; i < modelo.getColumnCount(); i++) {
                    csv.write(modelo.getColumnName(i) + (i == modelo.getColumnCount() - 1 ? "" : ","));
                }
                csv.write("\n");

                for (int i = 0; i < modelo.getRowCount(); i++) {
                    for (int j = 0; j < modelo.getColumnCount(); j++) {
                        csv.write(modelo.getValueAt(i, j).toString() + (j == modelo.getColumnCount() - 1 ? "" : ","));
                    }
                    csv.write("\n");
                }

                animarBarraProgreso();
                JOptionPane.showMessageDialog(delegado, "¡Archivo CSV exportado con éxito en:\n" + ruta);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(delegado, "Error al exportar el archivo CSV.");
            }
        }
    }

    private void animarBarraProgreso() {
        if (delegado.prg_barras != null) {
            delegado.prg_barras.setValue(0);
            new Thread(() -> {
                for (int i = 0; i <= 100; i += 25) {
                    final int progreso = i;
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException ignored) {}
                    delegado.prg_barras.setValue(progreso);
                }
            }).start();
        }
    }

    private void actualizarEstadisticas() {
        DefaultTableModel modeloTabla = (DefaultTableModel) delegado.tbl_contactos.getModel();
        int totalContactos = modeloTabla.getRowCount();
        int totalFavoritos = 0;

        for (int i = 0; i < totalContactos; i++) {
            if (modeloTabla.getColumnCount() > 7 && modeloTabla.getValueAt(i, 7) != null) {
                String esFavorito = modeloTabla.getValueAt(i, 7).toString();
                if (esFavorito.equalsIgnoreCase("Sí")) {
                    totalFavoritos++;
                }
            }
        }
    }

    private void limpiarCampos() {
        delegado.txt_nombre.setText("");
        delegado.txt_telefono.setText("");
        delegado.txt_cedula.setText("");
        delegado.txt_email.setText("");
        if (delegado.cmb_categoria.getItemCount() > 0) {
            delegado.cmb_categoria.setSelectedIndex(0);
        }
        delegado.chb_favorito.setSelected(false);
    }
}