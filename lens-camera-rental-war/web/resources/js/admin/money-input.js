// Xoá tất cả dấu chấm / ký tự không phải số trước khi gửi lên server
function cleanMoneyInputs() {
    var inputs = document.querySelectorAll(".money-input");
    inputs.forEach(function (input) {
        if (input.value) {
            input.value = input.value.replace(/\D/g, "");
        }
    });
}
window.cleanMoneyInputs = cleanMoneyInputs;

document.addEventListener("DOMContentLoaded", function () {
    var moneyInputs = document.querySelectorAll(".money-input");

    // Định dạng giá trị ban đầu khi mở form
    moneyInputs.forEach(function (input) {
        if (input.value) {
            var val = input.value.replace(/\D/g, "");
            input.value = val.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
        }

        //Định dạng trong lúc người dùng nhập
        input.addEventListener("input", function () {
            var val = this.value.replace(/\D/g, "");
            this.value = val.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
        });
    });

    // Tự động xoá dấu chấm trước khi submit trên tất cả các form có .money-input
    var forms = document.querySelectorAll("form");
    forms.forEach(function (form) {
        if (form.querySelector(".money-input")) {
            //Bắt sự kiện submit của form
            form.addEventListener("submit", function () {
                cleanMoneyInputs();
            });

            // Bắt sự kiện click trên các nút submit
            var submitButtons = form.querySelectorAll('input[type="submit"], button[type="submit"]');
            submitButtons.forEach(function (btn) {
                btn.addEventListener("click", function () {
                    cleanMoneyInputs();
                });
            });

            // Đề phòng gọi form.submit() trực tiếp qua JavaScript
            if (typeof form.submit === "function") {
                var originalSubmit = form.submit.bind(form);
                form.submit = function () {
                    cleanMoneyInputs();
                    originalSubmit();
                };
            }
        }
    });
});
