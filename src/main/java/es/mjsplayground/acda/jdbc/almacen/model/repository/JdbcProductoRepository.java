package es.mjsplayground.acda.jdbc.almacen.model.repository;

import es.mjsplayground.acda.jdbc.almacen.model.entity.Producto;
import java.sql.*;
import java.util.*;

public class JdbcProductoRepository {
  public List<Producto> listar(Connection c) throws SQLException {
    // TODO 2: consultar productos y mapear cada fila con ResultSet
    String url = "SELECT id,nombre,precio,stock FROM producto ORDER BY nombre";
    List<Producto> productos = new ArrayList<>();

    try (PreparedStatement p = c.prepareStatement(url); ResultSet r = p.executeQuery()) {
      while (r.next()){
        Producto prod = new Producto(
                r.getInt("id"),
                r.getString("nombre"),
                r.getInt("precio"),
                r.getInt("stock")
        );
        productos.add(prod);
      }
    }
    return productos;
    
    //throw new UnsupportedOperationException("Etapa pendiente: 2: consultar productos y mapear cada fila con ResultSet");
  }

  public Producto buscar(Connection c, long id) throws SQLException {
    // TODO 3: completar CRUD parametrizado sin cerrar la conexión recibida
    String url = "SELECT id, nombre, precio, stock FROM producto WHERE id = ?";

    try (PreparedStatement p = c.prepareStatement(url)) {
      p.setLong(1, id);
      try (ResultSet r = p.executeQuery()) {
        if (r.next()) {
          return new Producto(
              r.getInt("id"),
              r.getString("nombre"),
              r.getInt("precio"),
              r.getInt("stock"));
        }
      }
    }
    return null;

    //throw new UnsupportedOperationException("Etapa pendiente: 3: completar CRUD parametrizado sin cerrar la conexión recibida");
  }

  public long guardar(Connection c, Producto v) throws SQLException {
    // TODO 3: completar CRUD parametrizado sin cerrar la conexión recibida
    String sqlInsert = "INSERT INTO producto(nombre,precio,stock) VALUES(?,?,?)";
    String sqlUpdate = "UPDATE producto SET nombre = ?, precio = ?, stock = ? WHERE id = ?";

    if (v.id() == 0) { // Cuando no existe el producto se hace un INSERT
      try (PreparedStatement sentencia = c.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
        sentencia.setString(1, v.nombre());
        sentencia.setLong(2, v.precioCentimos());
        sentencia.setInt(3, v.stock());
        sentencia.executeUpdate();

        try (ResultSet rs = sentencia.getGeneratedKeys()) {
          if (rs.next()) {
            return rs.getLong(1);
          }
        }
      }
      throw new SQLException("No se pudo obtener el ID generado");
    } else { // Cuando existe el producto se hace un UPDATE
      try (PreparedStatement sentencia = c.prepareStatement(sqlUpdate)) {
        sentencia.setString(1, v.nombre());
        sentencia.setLong(2, v.precioCentimos());
        sentencia.setInt(3, v.stock());
        sentencia.setLong(4, v.id());
        if (sentencia.executeUpdate() == 0) {
          throw new SQLException("No existe el producto con ID: " + v.id());
        }
        return v.id();
      }
    }

    //throw new UnsupportedOperationException("Etapa pendiente: 3: completar CRUD parametrizado sin cerrar la conexión recibida");
  }

  public boolean descontar(Connection c, long id, int cantidad) throws SQLException {
    // TODO 4: descontar existencias solo si hay cantidad suficiente

    if (cantidad <= 0) {
      throw new SQLException("La cantidad debe ser mayor que cero");
    }

    String sql = "UPDATE producto SET stock = stock - ? WHERE id = ? AND stock >= ?";
    try (PreparedStatement sentencia = c.prepareStatement(sql)) {
      sentencia.setInt(1, cantidad);
      sentencia.setLong(2, id);
      sentencia.setInt(3, cantidad);
      return sentencia.executeUpdate() > 0;
    }

    // throw new UnsupportedOperationException("Etapa pendiente: 4: descontar existencias solo si hay cantidad suficiente");
  }

  public void eliminar(Connection c, long id) throws SQLException {
    // TODO 3: completar CRUD parametrizado sin cerrar la conexión recibida
    String sql = "DELETE FROM producto WHERE id = ?";

    try (PreparedStatement sentencia = c.prepareStatement(sql)) {
      sentencia.setLong(1, id);
      if (sentencia.executeUpdate() == 0) {
        throw new SQLException("No existe el producto con ID: " + id);
      }
    }
  }
}
