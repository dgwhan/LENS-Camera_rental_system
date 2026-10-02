document.addEventListener("DOMContentLoaded", () => {
    // Khởi tạo tất cả selector device model trên trang
    const selectors = document.querySelectorAll('[data-selector="device-model"]');
    selectors.forEach(initDeviceModelSelector);
});

function initDeviceModelSelector(selector) {
    // Lấy các phần tử DOM của selector
    const nativeSelect = selector.querySelector(".model-selector-native");
    const input = selector.querySelector(".model-combobox-input");
    const chevron = selector.querySelector(".model-combobox-chevron");
    const resultsContainer = selector.querySelector(".model-selector-results");

    if (!nativeSelect || !input || !resultsContainer) {
        return;
    }

    // Thu thập dữ liệu brand từ các thẻ metadata ẩn
    const brandMap = {};
    selector.querySelectorAll(".model-meta-data").forEach(el => {
        const id = el.getAttribute("data-id");
        if (!id) return;
        brandMap[id] = (el.getAttribute("data-brand") || "").trim();
    });

    // Lấy toàn bộ danh sách model từ thẻ select gốc của JSF
    const models = Array.from(nativeSelect.options)
        .filter(option => option.value !== "")
        .map(option => {
            const brand = option.getAttribute("data-brand")
                || option.title
                || brandMap[option.value]
                || "";

            return {
                id: option.value,
                label: option.textContent.trim(),
                brand: brand.trim()
            };
        });

    let filteredModels = [...models];

    // Khởi tạo giá trị ban đầu và hiển thị danh sách
    syncInputFromSelect();
    renderResults();

    // Click hoặc focus vào ô input để mở danh sách và xem toàn bộ
    input.addEventListener("click", () => {
        if (isOpen()) return;
        openDropdown();
    });

    input.addEventListener("focus", () => {
        openDropdown();
    });

    // Tìm kiếm nhanh trực tiếp khi gõ phím trên thanh
    input.addEventListener("input", () => {
        const keyword = input.value.trim().toLowerCase();
        if (!keyword) {
            filteredModels = [...models];
        } else {
            filteredModels = models.filter(m =>
                m.label.toLowerCase().includes(keyword) ||
                (m.brand && m.brand.toLowerCase().includes(keyword))
            );
        }

        if (!isOpen()) {
            openDropdown();
        }
        renderResults();
    });

    // Xử lý các phím điều hướng và phím tắt
    input.addEventListener("keydown", event => {
        if (event.key === "Escape") {
            closeDropdown();
            syncInputFromSelect();
            return;
        }

        if (event.key === "Enter") {
            event.preventDefault();
            if (filteredModels.length === 0) return;
            selectModel(filteredModels[0]);
            return;
        }

        if (event.key === "ArrowDown") {
            event.preventDefault();
            if (!isOpen()) {
                openDropdown();
                return;
            }
            const firstItem = resultsContainer.querySelector(".model-selector-item");
            if (firstItem) firstItem.focus();
        }
    });

    // Đóng hoặc mở danh sách khi bấm vào nút mũi tên
    if (chevron) {
        chevron.addEventListener("click", (e) => {
            e.stopPropagation();
            if (isOpen()) {
                closeDropdown();
                return;
            }
            input.focus();
            openDropdown();
        });
    }

    // Đóng danh sách khi click ra ngoài vùng selector
    document.addEventListener("click", event => {
        if (selector.contains(event.target)) return;
        closeDropdown();
        syncInputFromSelect();
    });

    // Đồng bộ lại ô input nếu thẻ select gốc của JSF thay đổi giá trị
    nativeSelect.addEventListener("change", () => {
        syncInputFromSelect();
    });

    // Kiểm tra trạng thái danh sách đang mở hay đóng
    function isOpen() {
        return selector.classList.contains("is-open");
    }

    // Mở danh sách kết quả
    function openDropdown() {
        selector.classList.add("is-open");
        filteredModels = [...models];
        renderResults();
    }

    // Đóng danh sách kết quả
    function closeDropdown() {
        selector.classList.remove("is-open");
    }

    // Đồng bộ giá trị hiển thị trên ô input theo giá trị đang chọn của thẻ select gốc
    function syncInputFromSelect() {
        const currentOption = nativeSelect.options[nativeSelect.selectedIndex];
        if (!currentOption || currentOption.value === "") {
            input.value = "";
            return;
        }
        input.value = currentOption.textContent.trim();
    }

    // Hiển thị danh sách kết quả ra giao diện (hiển thị toàn bộ, không phân trang)
    function renderResults() {
        resultsContainer.innerHTML = "";

        // Trạng thái không tìm thấy kết quả phù hợp
        if (filteredModels.length === 0) {
            const emptyState = document.createElement("div");
            emptyState.className = "model-selector-empty";
            emptyState.textContent = "No device models found.";
            resultsContainer.appendChild(emptyState);
            return;
        }

        // Render từng model thành một nút chọn
        filteredModels.forEach(model => {
            const item = document.createElement("button");
            item.type = "button";
            item.className = "model-selector-item";
            item.setAttribute("role", "option");
            item.setAttribute("data-value", model.id);

            item.innerHTML = `
                <span class="model-selector-name">${escapeHtml(model.label)}</span>
            `;

            // Đánh dấu mục đang được chọn
            if (nativeSelect.value === model.id) {
                item.classList.add("selected");
            }

            item.addEventListener("click", (e) => {
                e.stopPropagation();
                selectModel(model);
            });

            resultsContainer.appendChild(item);
        });
    }

    // Xử lý khi người dùng chọn một model
    function selectModel(model) {
        if (!model) return;

        // Cập nhật giá trị vào thẻ select gốc và kích hoạt sự kiện change
        nativeSelect.value = model.id;
        nativeSelect.dispatchEvent(new Event("change", { bubbles: true }));

        // Cập nhật nhãn hiển thị trên ô input
        input.value = model.label;
        filteredModels = [...models];
        closeDropdown();
    }

    // Hàm mã hóa an toàn các ký tự HTML đặc biệt
    function escapeHtml(value) {
        if (value == null) return "";
        const div = document.createElement("div");
        div.textContent = value;
        return div.innerHTML;
    }
}