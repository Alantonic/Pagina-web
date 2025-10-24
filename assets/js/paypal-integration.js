document.querySelectorAll('.btn-comprar').forEach(button => {
  button.addEventListener('click', async function () {
    const item = this.closest('.perfume-item');
    const producto = {
      id: item.dataset.id,
      nombre: item.dataset.nombre,
      precio: parseFloat(item.dataset.precio)
    };

    // 1. Crear orden en tu backend
    try {
      const res = await fetch('/backend-paypal/api/paypal/create-order', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre: producto.nombre,
          precio: producto.precio
        })
      });

      const data = await res.json();

      if (!data.orderId) {
        throw new Error(data.message || 'No se pudo crear la orden');
      }

      // 2. Redirigir a PayPal (o usar ventana emergente)
      const paypalUrl = `https://www.sandbox.paypal.com/checkoutnow?token=${data.orderId}`;
      window.location.href = paypalUrl;

      // Alternativa: abrir en nueva pestaña
      // window.open(paypalUrl, '_blank');

    } catch (error) {
      console.error('Error:', error);
      alert('Error al iniciar el pago: ' + error.message);
    }
  });
});