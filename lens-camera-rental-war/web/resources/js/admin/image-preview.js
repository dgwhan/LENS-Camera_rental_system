document.addEventListener("DOMContentLoaded", function () {

    var imageFile = document.getElementById("deviceModelForm:imageFile");

    if (!imageFile) {
        return;
    }

    // Ngăn chọn ảnh mới khi model đang có ảnh và chưa nhấn Remove
    imageFile.addEventListener("click", function (e) {
        var container = document.getElementById("currentImageContainer");
        var removeInput = document.getElementById("deviceModelForm:removeCurrentImage");
        var currentImg = document.getElementById("currentImage");
        var hasCustomImage = currentImg && currentImg.getAttribute("data-original-src");

        if (hasCustomImage
            && container
            && container.style.display !== "none"
            && removeInput
            && removeInput.value !== "true") {

            e.preventDefault();
            alert("Please remove the current image before choosing a new image.");
        }
    });

    // Xử lý khi người dùng chọn file ảnh
    imageFile.addEventListener("change", function () {

        if (!this.files || this.files.length === 0) {
            return;
        }

        var file = this.files[0];
        var extension = file.name.split(".").pop().toLowerCase();

        // Kiểm tra định dạng file
        if (!["jpg", "jpeg", "png", "webp"].includes(extension)) {
            alert("Only JPG, PNG, and WEBP files are allowed.");
            this.value = "";
            return;
        }

        // Kiểm tra kích thước file (tối đa 5MB)
        if (file.size > 5 * 1024 * 1024) {
            alert("Image size must not exceed 5MB.");
            this.value = "";
            return;
        }

        // Đặt lại cờ xóa ảnh trên server
        var removeInput = document.getElementById("deviceModelForm:removeCurrentImage");
        if (removeInput) {
            removeInput.value = "false";
        }

        var uploadFileName = document.getElementById("uploadFileName");
        if (uploadFileName) {
            uploadFileName.textContent = file.name;
        }
        var cfn = document.getElementById("currentImageFilename");
        if (cfn) {
            cfn.textContent = file.name;
        }

        // Đọc và hiển thị preview tức thì
        var reader = new FileReader();

        reader.onload = function (e) {
            var currentImg = document.getElementById("currentImage");
            var container = document.getElementById("currentImageContainer");
            var meta = document.getElementById("currentImageMeta");
            var label = document.getElementById("currentImageLabel");

            if (currentImg) {
                currentImg.src = e.target.result;
            }

            if (container) {
                container.style.display = "flex";
            }

            if (meta) {
                meta.style.display = "flex";
            }

            if (label) {
                label.textContent = "New image preview";
            }
        };

        reader.readAsDataURL(file);
    });
});

function removeCurrentImage() {

    var imageFile = document.getElementById("deviceModelForm:imageFile");
    var container = document.getElementById("currentImageContainer");
    var currentImg = document.getElementById("currentImage");
    var meta = document.getElementById("currentImageMeta");
    var label = document.getElementById("currentImageLabel");
    var newImageGroup = document.getElementById("newImageGroup");
    var removeInput = document.getElementById("deviceModelForm:removeCurrentImage");

    // Người dùng đang chọn một file mới -> nhấn Remove image để hủy chọn file đó
    if (imageFile && imageFile.files && imageFile.files.length > 0) {
        imageFile.value = "";

        var originalSrc = currentImg ? currentImg.getAttribute("data-original-src") : "";
        var wasRemoved = removeInput && removeInput.value === "true";

        // Nếu trước đó model có ảnh riêng ban đầu -> phục hồi lại ảnh cũ để hiển thị
        if (originalSrc && originalSrc.trim() !== "" && !wasRemoved) {
            currentImg.src = originalSrc;
            if (label) label.textContent = "Current image";
            if (newImageGroup) newImageGroup.style.display = "none";
            return;
        }

        // Nếu ở chế độ Create hoặc ảnh cũ đã bị xóa -> khôi phục ảnh mặc định
        var defaultSrc = currentImg ? currentImg.getAttribute("data-default-src") : "";
        if (defaultSrc) {
            currentImg.src = defaultSrc;
        }
        if (meta) {
            meta.style.display = "none";
        }
        return;
    }

    // Người dùng nhấn Remove image trên ảnh hiện tại của model
    if (removeInput) {
        removeInput.value = "true";
    }

    // Xóa file đã chọn nếu có
    if (imageFile) {
        imageFile.value = "";
    }

    var uploadFileName = document.getElementById("uploadFileName");
    if (uploadFileName) {
        uploadFileName.textContent = "No file chosen";
    }

    // Ẩn ảnh hiện tại
    if (container) {
        container.style.display = "none";
    }

    // Hiển thị phần chọn ảnh mới
    if (newImageGroup) {
        newImageGroup.style.display = "flex";
    }
}

window.removeCurrentImage = removeCurrentImage;