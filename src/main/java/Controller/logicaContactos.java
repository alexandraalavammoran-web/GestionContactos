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

 
 //MEJORAS DE CLASE 5: Controlador optimizado bajo el Patrón MVC.
 //Se añadio eventos avanzados, filtrado en tiempo real, exportación CSV.
 //animación de barra de progreso y manejo seguro de modificación/eliminación.
public class logicaContactos implements ActionListener {
    private VentanaContactos delegado;
    private TableRowSorter<DefaultTableModel> sorter;
    private int filaSeleccionada = -1;

    public logicaContactos(VentanaContactos delegado){
        this.delegado = delegado;
        
        // 1. Asignación de eventos a los botones de la interfaz mejorada
        if (this.delegado.btn_add != null) this.delegado.btn_add.addActionListener(this);
        if (this.delegado.btn_modificar != null) this.delegado.btn_modificar.addActionListener(this);
        if (this.delegado.btn_eliminar != null) this.delegado.btn_eliminar.addActionListener(this);
        if (this.delegado.btn_exportar != null) this.delegado.btn_exportar.addActionListener(this);

        // Mejora: Evento de ratón en la JTable para selección y carga automática de datos
        this.delegado.tbl_contactos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarContactoSeleccionado();
            }
        });

        configurarBuscador();
        actualizarEstadisticas();
    }

    // Mejora: La Configuración de TableRowSorter para filtrado dinámico (requisito de la rúbrica)
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
        if (delegado.btn_add != null && e.getSource() == delegado.btn_add){
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
        String telefono = delegado.txt_telefono.getText().trim();
        String email = delegado.txt_email.getText().trim();
        String categoria = delegado.cmb_categoria.getSelectedItem().toString();
        boolean favorito = delegado.chb_favorito.isSelected();

        if (!nombres.isEmpty() && !telefono.isEmpty() && !email.isEmpty()){
            if (!categoria.equalsIgnoreCase("Elija una categoría") && !categoria.equalsIgnoreCase("Item 1") && !categoria.equalsIgnoreCase("(defaut)") ){

                try {
                    Persona persona = new Persona(nombres, telefono, email, categoria, favorito);
                    PersonaDAO dao = new PersonaDAO(persona);
                    dao.escribirArchivo();

                    DefaultTableModel modeloTabla = (DefaultTableModel) delegado.tbl_contactos.getModel();
                    String esFavoritoTexto = favorito ? "Sí" : "No";
                    modeloTabla.addRow(new Object[]{nombres, telefono, email, categoria, esFavoritoTexto});

                    // Mejora: La activación de la barra de progreso en cada inserción exitosa
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

    private void cargarContactoSeleccionado() {
        int filaVis = delegado.tbl_contactos.getSelectedRow();
        if (filaVis >= 0) {
            filaSeleccionada = delegado.tbl_contactos.convertRowIndexToModel(filaVis);
            DefaultTableModel modelo = (DefaultTableModel) delegado.tbl_contactos.getModel();
            
            delegado.txt_nombre.setText(modelo.getValueAt(filaSeleccionada, 0).toString());
            delegado.txt_telefono.setText(modelo.getValueAt(filaSeleccionada, 1).toString());
            delegado.txt_email.setText(modelo.getValueAt(filaSeleccionada, 2).toString());
            delegado.cmb_categoria.setSelectedItem(modelo.getValueAt(filaSeleccionada, 3).toString());
            
            String fav = modelo.getValueAt(filaSeleccionada, 4).toString();
            delegado.chb_favorito.setSelected(fav.equalsIgnoreCase("Sí"));
        }
    }

    // Mejora: Modificación segura y directa seleccionando la fila sin sobreescribir celdas vacías
    private void guardarModificacionContacto() {
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(delegado, "Por favor, haga clic en un contacto de la tabla para modificarlo.");
            return;
        }

        String nombres = delegado.txt_nombre.getText().trim();
        String telefono = delegado.txt_telefono.getText().trim();
        String email = delegado.txt_email.getText().trim();
        String categoria = delegado.cmb_categoria.getSelectedItem().toString();
        boolean favorito = delegado.chb_favorito.isSelected();

        if (!nombres.isEmpty() && !telefono.isEmpty() && !email.isEmpty()) {
            DefaultTableModel modelo = (DefaultTableModel) delegado.tbl_contactos.getModel();

            modelo.setValueAt(nombres, filaSeleccionada, 0);
            modelo.setValueAt(telefono, filaSeleccionada, 1);
            modelo.setValueAt(email, filaSeleccionada, 2);
            modelo.setValueAt(categoria, filaSeleccionada, 3);
            modelo.setValueAt(favorito ? "Sí" : "No", filaSeleccionada, 4);

            animarBarraProgreso();
            actualizarEstadisticas();
            JOptionPane.showMessageDialog(delegado, "¡Contacto modificado con éxito!");
            limpiarCampos();
            filaSeleccionada = -1;
        } else {
            JOptionPane.showMessageDialog(delegado, "Los campos no pueden estar vacíos al modificar.");
        }
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
            filaSeleccionada = -1;
        }
    }

    // Mejora: Funcionalidad avanzada de exportación a CSV con selector de archivos del sistema operativo
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

    // Mejora: Hilos concurrentes (Threads) para animar la JProgressBar en operaciones del sistema
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

    // Mejora: Actualización dinámica en tiempo real de estadísticas para la pestaña de Estadísticas
    private void actualizarEstadisticas() {
        DefaultTableModel modeloTabla = (DefaultTableModel) delegado.tbl_contactos.getModel();
        int totalContactos = modeloTabla.getRowCount();
        int totalFavoritos = 0;

        for (int i = 0; i < totalContactos; i++) {
            if (modeloTabla.getColumnCount() > 4 && modeloTabla.getValueAt(i, 4) != null) {
                String esFavorito = modeloTabla.getValueAt(i, 4).toString();
                if (esFavorito.equalsIgnoreCase("Sí")) {
                    totalFavoritos++;
                }
            }
        }

        if (delegado.lbl_total != null) delegado.lbl_total.setText("Total de Contactos: " + totalContactos);
        if (delegado.lbl_favoritos != null) delegado.lbl_favoritos.setText("Contactos Favoritos: " + totalFavoritos);
    }

    private void limpiarCampos() {
        delegado.txt_nombre.setText("");
        delegado.txt_telefono.setText("");
        delegado.txt_email.setText("");
        if (delegado.cmb_categoria.getItemCount() > 0) {
            delegado.cmb_categoria.setSelectedIndex(0);
        }
        delegado.chb_favorito.setSelected(false);
    }
}