function openModal(typeName) {
    const allTbody = document.querySelectorAll(".modal-body tbody");
    allTbody.forEach(tbody => {
        tbody.style.display = "none";
    });
    
    const findTbody = document.getElementById(typeName);
    if (findTbody) {
        findTbody.style.display = "table-row-group";
    } else {
        console.error(`${typeName}에 해당하는 상세정보가 존재하지 않습니다.`);
    }

    document.getElementById("modal-background").style.display = "block";
    document.getElementById("modal-container").style.display = "block";
}

function closeModal() {
    document.getElementById("modal-background").style.display = "none";
    document.getElementById("modal-container").style.display = "none";
}
