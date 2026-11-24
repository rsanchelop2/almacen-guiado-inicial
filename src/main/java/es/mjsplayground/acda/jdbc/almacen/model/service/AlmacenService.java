package es.mjsplayground.acda.jdbc.almacen.model.service;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.entity.*;
import es.mjsplayground.acda.jdbc.almacen.model.repository.*;
import java.sql.*;
import java.util.*;
import org.slf4j.*;

public class AlmacenService {
  private static final Logger log = LoggerFactory.getLogger(AlmacenService.class);
  private final Database db;
  private final JdbcProductoRepository productos;
  private final JdbcVentaRepository ventas;

  public AlmacenService(Database db) {
    this(db, new JdbcProductoRepository(), new JdbcVentaRepository());
  }

  public AlmacenService(Database db, JdbcProductoRepository productos, JdbcVentaRepository ventas) {
    this.db = db;
    this.productos = productos;
    this.ventas = ventas;
  }

  public List<Producto> listar() {
    try (Connection c = db.abrir()) {
      return productos.listar(c);
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  public List<Venta> ventas() {
    try (Connection c = db.abrir()) {
      return ventas.listar(c);
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  public long guardar(Producto p) {
    // TODO 3: validar nombre, precio y stock antes de delegar la escritura
    if (p == null) {
      throw new ReglaNegocioException("El producto no puede ser nulo.");
    }
    if (p.nombre() == null || p.nombre().isBlank()) {
      throw new ReglaNegocioException("El nombre del producto es obligatorio.");
    }
    if (p.nombre().trim().length() > 80) {
      throw new ReglaNegocioException("El nombre no puede superar los 80 caracteres.");
    }
    if (p.precioCentimos() <= 0 || p.precioCentimos() > 100000000) {
      throw new ReglaNegocioException("El precio debe estar entre 1 y 100.000.000 céntimos.");
    }
    if (p.stock() < 0) {
      throw new ReglaNegocioException("El stock no puede ser negativo.");
    }

    Producto normalizado = new Producto(
            p.id(),
            p.nombre().trim(),
            p.precioCentimos(),
            p.stock()
    );

    try (Connection c = db.abrir()) {
      return productos.guardar(c, normalizado);
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  public void eliminar(long id) {
    try (Connection c = db.abrir()) {
      if (ventas.existenPara(c, id))
        throw new ReglaNegocioException("Conserva los productos que tienen ventas registradas.");
      productos.eliminar(c, id);
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  public long vender(long id, int cantidad) {
    if (cantidad <= 0) {
      throw new ReglaNegocioException("La cantidad debe ser mayor que cero.");
    }

    try (Connection c = db.abrir()) {
      c.setAutoCommit(false);
      try {
        Producto p = productos.buscar(c, id);
        if (p == null) {
          throw new ReglaNegocioException("El producto no existe.");
        }
        if (p.stock() < cantidad) {
          throw new ReglaNegocioException("No hay stock suficiente.");
        }
        if (!productos.descontar(c, id, cantidad)) {
          throw new ReglaNegocioException("No hay stock suficiente.");
        }

        long total = p.precioCentimos() * cantidad;
        ventas.registrar(c, id, cantidad, total);
        c.commit();
        return total;
      } catch (ReglaNegocioException e) {
        c.rollback();
        throw e;
      } catch (SQLException e) {
        c.rollback();
        throw fallo(e);
      }
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  private PersistenciaException fallo(SQLException e) {
    log.error("Fallo de persistencia en almacén", e);
    return new PersistenciaException(
        "No se pudo guardar o consultar el almacén. Comprueba nombres duplicados, permisos y el"
            + " registro técnico.",
        e);
  }
}
