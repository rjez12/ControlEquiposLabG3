package com.desarrollo.controlequiposlabg3.controller;

import com.desarrollo.controlequiposlabg3.dao.EquipoDAO;
import com.desarrollo.controlequiposlabg3.dao.TipoEquipoDAO;
import com.desarrollo.controlequiposlabg3.model.Equipo;
import com.desarrollo.controlequiposlabg3.model.TipoEquipo;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EquipoController {

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtMarca;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<TipoEquipo> cmbTipo;

    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private ComboBox<TipoEquipo> cmbFiltroTipo;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnEliminar;

    @FXML
    private Label lblEstadoFormulario;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<Equipo> tablaEquipos;

    @FXML
    private TableColumn<Equipo, String> colCodigo;

    @FXML
    private TableColumn<Equipo, String> colNombre;

    @FXML
    private TableColumn<Equipo, String> colMarca;

    @FXML
    private TableColumn<Equipo, String> colTipo;

    @FXML
    private TableColumn<Equipo, String> colEstado;

    @FXML
    private TableColumn<Equipo, Integer> colCantidad;

    private final EquipoDAO equipoDAO = new EquipoDAO();
    private final TipoEquipoDAO tipoEquipoDAO = new TipoEquipoDAO();

    private final List<String> estadosPermitidos = List.of(
            "Disponible",
            "En mantenimiento",
            "Fuera de servicio"
    );

    // null indica que no hay un equipo seleccionado.
    private Long idSeleccionado;

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(
                new PropertyValueFactory<>("codigo")
        );

        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre")
        );

        colMarca.setCellValueFactory(
                new PropertyValueFactory<>("marca")
        );

        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado")
        );

        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad")
        );

        colTipo.setCellValueFactory(data -> {
            TipoEquipo tipoEquipo = data.getValue().getTipoEquipo();

            String nombre = tipoEquipo != null
                    ? tipoEquipo.getNombre()
                    : "Sin tipo";

            return new SimpleStringProperty(nombre);
        });

        tablaEquipos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        cmbEstado.setItems(
                FXCollections.observableArrayList(estadosPermitidos)
        );

        cmbEstado.setEditable(false);
        cmbTipo.setEditable(false);
        cmbFiltroTipo.setEditable(false);

        tablaEquipos.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        seleccionarEquipo(newValue);
                    } else {
                        idSeleccionado = null;
                        actualizarBotones();
                    }
                });

        limpiar();

        try {
            cargarTipos();
            cargarEquipos();

        } catch (Exception ex) {
            mostrarError(
                    "No se pudieron cargar los datos iniciales. "
                            + "Revise la conexión con PostgreSQL.",
                    ex
            );
        }
    }

    private void cargarTipos() {
        List<TipoEquipo> tipos = tipoEquipoDAO.listar();

        cmbTipo.setItems(
                FXCollections.observableArrayList(tipos)
        );

        cmbFiltroTipo.setItems(
                FXCollections.observableArrayList(tipos)
        );
    }

    private void seleccionarEquipo(Equipo equipo) {
        idSeleccionado = equipo.getId();

        txtCodigo.setText(equipo.getCodigo());
        txtNombre.setText(equipo.getNombre());

        txtMarca.setText(
                equipo.getMarca() == null ? "" : equipo.getMarca()
        );

        txtCantidad.setText(
                String.valueOf(equipo.getCantidad())
        );

        cmbEstado.setValue(equipo.getEstado());

        cmbTipo.getSelectionModel().clearSelection();

        // Seleccionar el tipo del ComboBox comparando sus identificadores.
        for (TipoEquipo tipo : cmbTipo.getItems()) {
            if (equipo.getTipoEquipo() != null
                    && tipo.getId().equals(
                    equipo.getTipoEquipo().getId()
            )) {

                cmbTipo.setValue(tipo);
                break;
            }
        }

        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean sinSeleccion = idSeleccionado == null;

        btnGuardar.setDisable(!sinSeleccion);
        btnActualizar.setDisable(sinSeleccion);
        btnEliminar.setDisable(sinSeleccion);

        lblEstadoFormulario.setText(
                sinSeleccion ? "Nuevo equipo" : "Editar equipo"
        );
    }

    private Equipo obtenerEquipoFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String marca = txtMarca.getText().trim();
        String textoCantidad = txtCantidad.getText().trim();

        TipoEquipo tipoEquipo = cmbTipo.getValue();
        String estado = cmbEstado.getValue();

        if (codigo.isEmpty() || nombre.isEmpty()
                || textoCantidad.isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe completar código, nombre y cantidad."
            );
        }

        if (codigo.length() > 30) {
            throw new IllegalArgumentException(
                    "El código no puede superar los 30 caracteres."
            );
        }

        if (nombre.length() > 150) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 150 caracteres."
            );
        }

        if (marca.length() > 100) {
            throw new IllegalArgumentException(
                    "La marca no puede superar los 100 caracteres."
            );
        }

        if (tipoEquipo == null || tipoEquipo.getId() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un tipo de equipo."
            );
        }

        if (estado == null || !estadosPermitidos.contains(estado)) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un estado permitido."
            );
        }

        int cantidad;

        try {
            cantidad = Integer.parseInt(textoCantidad);

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser un número entero válido "
                            + "entre 0 y 2147483647."
            );
        }

        if (cantidad < 0) {
            throw new IllegalArgumentException(
                    "La cantidad no puede ser negativa."
            );
        }

        // Se crea un objeto nuevo para no alterar la fila
        // antes de confirmar los cambios en PostgreSQL.
        Equipo equipo = new Equipo();

        equipo.setCodigo(codigo);
        equipo.setNombre(nombre);
        equipo.setMarca(marca.isEmpty() ? null : marca);
        equipo.setCantidad(cantidad);
        equipo.setEstado(estado);
        equipo.setTipoEquipo(tipoEquipo);

        return equipo;
    }

    @FXML
    private void guardar() {
        if (idSeleccionado != null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Equipo seleccionado",
                    "Presione Limpiar para registrar un equipo nuevo."
            );
            return;
        }

        try {
            Equipo equipo = obtenerEquipoFormulario();

            EquipoDAO.guardar(equipo);

        } catch (IllegalArgumentException ex) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Revise los datos",
                    ex.getMessage()
            );
            return;

        } catch (Exception ex) {
            mostrarError("No se pudo guardar el equipo.", ex);
            return;
        }

        finalizarOperacion("El equipo se guardó correctamente.");
    }

    @FXML
    private void actualizar() {
        if (idSeleccionado == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Seleccione un equipo",
                    "Debe seleccionar el equipo que desea actualizar."
            );
            return;
        }

        try {
            Equipo equipo = obtenerEquipoFormulario();
            equipo.setId(idSeleccionado);

            boolean modificado = equipoDAO.modificar(equipo);

            if (!modificado) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Actualización no realizada",
                        "El equipo seleccionado ya no existe."
                );
                return;
            }

        } catch (IllegalArgumentException ex) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Revise los datos",
                    ex.getMessage()
            );
            return;

        } catch (Exception ex) {
            mostrarError("No se pudo actualizar el equipo.", ex);
            return;
        }

        finalizarOperacion("El equipo se actualizó correctamente.");
    }

    @FXML
    private void eliminar() {
        if (idSeleccionado == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Seleccione un equipo",
                    "Debe seleccionar el equipo que desea eliminar."
            );
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmacion.setTitle("Confirmación de eliminación");
        confirmacion.setHeaderText("Eliminar equipo");
        confirmacion.setContentText(
                "¿Desea eliminar el equipo "
                        + txtCodigo.getText()
                        + " — " + txtNombre.getText()
                        + "?\nEsta acción no se puede deshacer."
        );

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isEmpty()
                || resultado.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean eliminado = EquipoDAO.eliminar(idSeleccionado);

            if (!eliminado) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Eliminación no realizada",
                        "El equipo seleccionado ya no existe."
                );
                return;
            }

        } catch (Exception ex) {
            mostrarError("No se pudo eliminar el equipo.", ex);
            return;
        }

        finalizarOperacion("El equipo se eliminó correctamente.");
    }

    private void cargarEquipos() {
        String nombre = txtBuscar.getText().trim();
        TipoEquipo tipoEquipo = cmbFiltroTipo.getValue();

        List<Equipo> equipos;

        if (nombre.isEmpty() && tipoEquipo == null) {
            equipos = equipoDAO.listar();
        } else {
            equipos = equipoDAO.buscar(nombre, tipoEquipo);
        }

        tablaEquipos.setItems(
                FXCollections.observableArrayList(equipos)
        );

        lblTotal.setText(
                equipos.size() == 1
                        ? "1 resultado"
                        : equipos.size() + " resultados"
        );
    }

    @FXML
    private void buscar() {
        try {
            cargarEquipos();
            limpiar();

        } catch (Exception ex) {
            mostrarError(
                    "No se pudo consultar el inventario. "
                            + "La tabla conserva los datos anteriores.",
                    ex
            );
        }
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroTipo.getSelectionModel().clearSelection();
        cmbFiltroTipo.setValue(null);

        buscar();
    }

    @FXML
    private void limpiar() {
        tablaEquipos.getSelectionModel().clearSelection();

        idSeleccionado = null;

        txtCodigo.clear();
        txtNombre.clear();
        txtMarca.clear();
        txtCantidad.clear();

        cmbTipo.getSelectionModel().clearSelection();
        cmbTipo.setValue(null);

        cmbEstado.setValue("Disponible");

        actualizarBotones();
    }

    private void finalizarOperacion(String mensaje) {
        limpiar();

        try {
            // Mantener los filtros actuales al recargar.
            cargarEquipos();

        } catch (Exception ex) {
            ex.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Cambio guardado",
                    mensaje
                            + "\n\nEl cambio fue confirmado en PostgreSQL, "
                            + "pero no se pudo actualizar la tabla. "
                            + "Presione Aplicar filtros para reintentar."
            );
            return;
        }

        mostrarAlerta(
                Alert.AlertType.INFORMATION,
                "Operación realizada",
                mensaje
        );
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje
    ) {
        Alert alerta = new Alert(tipo);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje, Exception ex) {
        ex.printStackTrace();

        String detalle =
                "Revise la conexión y la configuración de PostgreSQL.";

        // Buscar errores de PostgreSQL dentro de la excepción de Hibernate.
        Throwable causa = ex;

        while (causa != null) {
            if (causa instanceof SQLException errorSQL) {
                String estadoSQL = errorSQL.getSQLState();

                if ("23505".equals(estadoSQL)) {
                    detalle = "Ya existe un equipo con ese código. "
                            + "Utilice un código diferente.";

                } else if ("23503".equals(estadoSQL)) {
                    detalle = "El registro está relacionado con datos "
                            + "que ya no existen o que impiden la operación.";

                } else if ("23514".equals(estadoSQL)) {
                    detalle = "La cantidad o el estado incumplen "
                            + "las restricciones de la base de datos.";

                } else if ("23502".equals(estadoSQL)) {
                    detalle = "Falta un dato obligatorio.";
                }

                break;
            }

            causa = causa.getCause();
        }

        mostrarAlerta(
                Alert.AlertType.ERROR,
                "Operación no realizada",
                mensaje + "\n\n" + detalle
        );
    }
}