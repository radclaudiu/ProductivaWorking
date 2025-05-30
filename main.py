from app import app

# Para gunicorn, solo importamos la aplicación
# Los servicios en segundo plano se inicializan en app.py

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=True)