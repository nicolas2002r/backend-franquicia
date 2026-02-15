package co.com.bancolombia.usecase.shared;

import co.com.bancolombia.usecase.franchise.shared.Messages;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {

    @Test
    void shouldExposeExpectedConstants() {
        assertEquals("No se encontró sucursal para franquicia", Messages.NO_SUCURSAL);
        assertEquals("Producto no encontrado en la sucursal", Messages.PRODUCTO_NO_ENCONTRADO);
        assertEquals("Franquicia no encontrada: ", Messages.FRANQUICIA_NO_ENCONTRADA);

        assertEquals("Franchise name is required", Messages.FRANCHISE_NAME_REQUIRED);
        assertEquals("Branch name is required", Messages.BRANCH_NAME_REQUIRED);
        assertEquals("Product name is required", Messages.PRODUCT_NAME_REQUIRED);
        assertEquals("Franchise name already exists", Messages.FRANCHISE_NAME_EXISTS);
        assertEquals("Stock cannot be negative", Messages.STOCK_NEGATIVE);
    }

    @Test
    void shouldNotAllowInstantiation() throws Exception {
        Constructor<Messages> ctor = Messages.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, ctor::newInstance);
        assertTrue(ex.getCause() instanceof UnsupportedOperationException
                        || ex.getCause() instanceof IllegalAccessException
                        || ex.getCause() instanceof RuntimeException
                        || ex.getCause() instanceof Exception,
                "Expected constructor to be inaccessible/throw when invoked reflectively");
    }
}
