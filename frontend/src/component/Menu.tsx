import { Button } from "@mui/material";
import { useNavigate } from "react-router-dom";
import "./Menu.css";

const Menu = () => {
    let navigate = useNavigate();

    const onLogout = () => {
        localStorage.removeItem("user");
        navigate("/login");
    };

    return <div className="logout-button-wrapper">
        <Button variant="outlined" onClick={onLogout}>Logout</Button>
    </div>;
}

export default Menu;