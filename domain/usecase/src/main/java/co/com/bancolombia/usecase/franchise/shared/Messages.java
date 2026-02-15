package co.com.bancolombia.usecase.franchise.shared;


public final class Messages {
    private Messages() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String NO_SUCURSAL = "No se encontró sucursal para franquicia";
    public static final String PRODUCTO_NO_ENCONTRADO = "Producto no encontrado en la sucursal";
    public static final String FRANQUICIA_NO_ENCONTRADA = "Franquicia no encontrada: ";

    public static final String FRANCHISE_NAME_REQUIRED = "Franchise name is required";
    public static final String BRANCH_NAME_REQUIRED = "Branch name is required";
    public static final String PRODUCT_NAME_REQUIRED = "Product name is required";
    public static final String FRANCHISE_NAME_EXISTS = "Franchise name already exists";
    public static final String STOCK_NEGATIVE = "Stock cannot be negative";
}

