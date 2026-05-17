import httpClient from "../http-common";

const processPayment = (data) => {
    return httpClient.post("/payments/process", data);
};

const getPaymentByBooking = (id) => {
    return httpClient.get(`/payments/booking/${id}`);
};

export default { processPayment, getPaymentByBooking };