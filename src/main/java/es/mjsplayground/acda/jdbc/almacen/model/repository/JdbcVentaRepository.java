package es.mjsplayground.acda.jdbc.almacen.model.repository;

import es.mjsplayground.acda.jdbc.almacen.model.entity.Venta;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JdbcVentaRepository {
  public boolean existenPara(Connection c, long id) throws SQLException {
    try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM venta WHERE producto_id=?")) {
      p.setLong(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next();
      }
    }
  }

  public void registrar(Connection c, long producto, int cantidad, long total) throws SQLException {
    // TODO 4: registrar la venta usando la conexión recibida
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String fecha = java.time.LocalDateTime.now().format(formatter);

    if (existenPara(c, producto)) {
      String sql = "UPDATE venta SET cantidad = cantidad + ?, total = total + ?, fecha = ? WHERE producto_id = ?";
      try (PreparedStatement p = c.prepareStatement(sql)) {
        p.setInt(1, cantidad);
        p.setLong(2, total);
        p.setString(3, fecha);
        p.setLong(4, producto);
        int actualizados = p.executeUpdate();
        if (actualizados == 0) {
          throw new SQLException("No se pudo actualizar la venta para el producto con ID: " + producto);
        }
      }
    } else {
      String sql = "INSERT INTO venta (producto_id, cantidad, total, fecha) VALUES (?, ?, ?, ?)";
      try (PreparedStatement p = c.prepareStatement(sql)) {
        p.setLong(1, producto);
        p.setInt(2, cantidad);
        p.setLong(3, total);
        p.setString(4, fecha);
        int insertados = p.executeUpdate();
        if (insertados == 0) {
          throw new SQLException("No se pudo registrar la venta para el producto con ID: " + producto);
        }
      }
    }

    //throw new UnsupportedOperationException("Etapa pendiente: 4: registrar la venta usando la conexión recibida");

  }

  public List<Venta> listar(Connection c) throws SQLException {
    List<Venta> datos = new ArrayList<>();
    try (PreparedStatement p =
            c.prepareStatement(
                "SELECT v.id,p.nombre,v.cantidad,v.total,v.fecha FROM venta v JOIN producto p ON"
                    + " p.id=v.producto_id ORDER BY v.id DESC");
        ResultSet r = p.executeQuery()) {
      while (r.next())
        datos.add(
            new Venta(r.getLong(1), r.getString(2), r.getInt(3), r.getLong(4), r.getString(5)));
    }
    return datos;
  }
}
