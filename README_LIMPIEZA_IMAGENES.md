# Sistema de Limpieza Automática de Imágenes

Este documento explica el sistema de limpieza automática de imágenes de recibos de gastos en Productiva.

## Funcionamiento

El script `cleanup_receipt_images.py` elimina automáticamente las imágenes de recibos de gastos que:

1. Tienen más de 10 días de antigüedad (configurable en `config.py`)
2. No están siendo utilizadas en ningún registro de gasto en la base de datos

## Ubicación de las imágenes

Las imágenes de recibos se almacenan en la siguiente estructura:

```
/uploads/expense_receipts/{company_id}/{timestamp}_{nombre_original}.jpg
```

Por ejemplo:
```
/uploads/expense_receipts/8/20250425132145_factura_proveedor.jpg
```

## Configuración

La configuración de retención se puede ajustar modificando el parámetro `RECEIPT_IMAGES_RETENTION_DAYS` en el archivo `config.py`. Por defecto, las imágenes se conservan durante 10 días.

## Ejecución

Para ejecutar la limpieza manualmente:

```bash
python cleanup_receipt_images.py
```

## Automatización (Recomendado)

Para automatizar la limpieza, configura un cron job que ejecute el script diariamente. Por ejemplo:

1. Abre el editor de cron:
   ```bash
   crontab -e
   ```

2. Añade una línea para ejecutar el script a las 3:00 AM todos los días:
   ```
   0 3 * * * cd /ruta/a/tu/aplicacion && /ruta/a/tu/venv/bin/python cleanup_receipt_images.py >> /tmp/cleanup_receipt_images.log 2>&1
   ```

3. Guarda y cierra.

## Seguridad

El script incluye las siguientes medidas de seguridad:

- Verifica que las imágenes no estén en uso antes de eliminarlas
- Solo elimina archivos de imagen, no directorios
- Logs detallados de todas las operaciones realizadas
- En caso de error, no elimina el archivo (preferible mantenerlo que perderlo)

## Logs

El script genera logs detallados indicando:
- Cuándo comienza y termina el proceso
- Cuántos archivos ha revisado
- Cuántos archivos ha eliminado
- Errores encontrados (si los hay)

## Solución de problemas

Si el script no elimina archivos como se espera, revisa:

1. Que los archivos tengan más de `RECEIPT_IMAGES_RETENTION_DAYS` días
2. Que los archivos no estén siendo utilizados por registros en la base de datos
3. Que el script tenga permisos de escritura en el directorio `/uploads/expense_receipts/`

Para depuración, puedes ejecutar el script manualmente y revisar la salida.