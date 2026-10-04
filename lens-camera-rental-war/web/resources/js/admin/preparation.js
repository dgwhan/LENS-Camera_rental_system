function openStorePickupFailureModal() {
    const modal = document.getElementById(
        'orderDetailForm:storePickupFailureModal'
    );

    if (modal) {
        modal.style.display = 'flex';
    }
}

function closeStorePickupFailureModal() {
    const modal = document.getElementById(
        'orderDetailForm:storePickupFailureModal'
    );

    if (modal) {
        modal.style.display = 'none';
    }
}

function openDeliveryFailureModal() {
    const modal = document.getElementById(
        'orderDetailForm:deliveryFailureModal'
    );

    if (modal) {
        modal.style.display = 'flex';
    }
}

function closeDeliveryFailureModal() {
    const modal = document.getElementById(
        'orderDetailForm:deliveryFailureModal'
    );

    if (modal) {
        modal.style.display = 'none';
    }
}