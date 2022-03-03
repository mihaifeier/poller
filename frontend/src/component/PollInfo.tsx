import AddIcon from '@mui/icons-material/Add';
import { Button, Card, Container, Dialog, DialogTitle, Fade, Snackbar, TextField } from "@mui/material";
import { useEffect, useState } from "react";
import SockJS from "sockjs-client";
import ErrorInterface from '../model/ErrorInterface';
import PollInterface from "../model/PollInterface";
import PollStatusEnum from '../model/enum/PollStatusEnum';
import "./PollInfo.css";

const PollInfo = () => {
    const [polls, setPolls] = useState({} as { [key: string]: PollInterface });
    const [modal, setModal] = useState({ open: false, create: true, name: "", url: "", poll: {} as PollInterface });
    const [snackbar, setSnackbar] = useState({ open: false, message: "" });

    const sockJsConnection = new SockJS("http://localhost:8080/live-update");
    sockJsConnection.onopen = () => {
        const user = localStorage.getItem("user");
        sockJsConnection.send(user ? user : "");

    };

    sockJsConnection.onmessage = (event) => {
        if (Object.keys(polls).length > 0) {
            const decodedPoll: PollInterface = JSON.parse(event.data);
            decodedPoll.active = true;
            setPolls(polls => {return {...polls, [decodedPoll.id]: decodedPoll}});
        }
    };

    useEffect(() => {
        const user = localStorage.getItem("user");
        fetch("http://localhost:8080/poll", {
            method: "GET",
            headers: { "Content-Type": "application/json", user: user ? user : "" },
        })
            .then(response => response.json())
            .then(response => {
                let pollsById: { [key: number]: PollInterface } = {};
                response.forEach((poll: PollInterface) => {
                    poll.active = true;
                    pollsById[poll.id] = poll;
                });

                setPolls(pollsById);
            });

        return () => {
            sockJsConnection.close();
        }
    }, []);

    const resetModal = () => {
        setModal({ open: false, create: true, name: "", url: "", poll: {} as PollInterface });
    }

    const savePoll = () => {
        if (modal.create) {
            addPoll(modal.name, modal.url, localStorage.getItem("user"));
        } else {
            updatePoll(modal.poll, modal.name, modal.url);
        }
    }

    const addPoll = (name: string, url: string, user: string | null) => {
        fetch("http://localhost:8080/poll", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, url, user })
        })
            .then(response => response.json())
            .then((poll: PollInterface | ErrorInterface) => {
                if ('error' in poll) {
                    setSnackbar({ open: true, message: poll.error });
                } else {
                    poll.active = true;
                    setPolls(polls => {return {...polls, [poll.id]: poll}});
                    resetModal();
                }

            });
    }

    const updatePoll = (poll: PollInterface, name: string, url: string) => {
        let tempPoll = { ...poll };
        tempPoll.name = name;
        tempPoll.url = url;
        delete tempPoll["active"];

        fetch("http://localhost:8080/poll", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(tempPoll)
        })
            .then(response => response.json())
            .then((poll: PollInterface | ErrorInterface) => {
                if ('error' in poll) {
                    setSnackbar({ open: true, message: poll.error });
                } else {
                    poll.active = true;
                    setPolls(polls => {return {...polls, [poll.id]: poll}});
                    resetModal();
                }
            });
    }

    const onDeletePoll = (poll: PollInterface) => {
        fetch(`http://localhost:8080/poll/${poll.id}`, { method: "DELETE" })
            .then(response => response.ok ? null : response.json())
            .then((response: null | ErrorInterface) => {
                if (response) {
                    setSnackbar({ open: true, message: response.error });
                } else {
                    let tempPolls = { ...polls };
                    tempPolls[poll.id] = { ...poll, active: false };
                    setPolls(tempPolls);
                    setModal({ ...modal, open: false });

                    setTimeout(() => {
                        setPolls(polls => {
                            let tempPolls = { ...polls };
                            delete tempPolls[poll.id];
                            return tempPolls;
                        });
                    }, 1500);
                }
            });
    }

    return (
        <Container fixed>
            <div className="cards-wrapper">
                <Card
                    className="card add-card"
                    variant="outlined"
                    onClick={() => setModal({ ...modal, open: true, create: true })}>
                    <AddIcon sx={{ fontSize: 60 }} />
                </Card>

                {Object.keys(polls).map(pollId => (
                    <Fade timeout={1500} key={polls[pollId].id} in={polls[pollId].active}>
                        <Card className="card" variant="outlined">
                            <div className={
                                `header ${polls[pollId].pollStatus === PollStatusEnum.OK ? "header-ok" : "header-fail"}`
                            }>
                                {polls[pollId].name}
                            </div>
                            <div className="body">
                                <div><span className="bold">URL:</span> {polls[pollId].url}</div>
                                <div><span className="bold">Status:</span> {polls[pollId].pollStatus}</div>
                                <div><span className="bold">Creation date:</span> {polls[pollId].createdAt}</div>
                                <div><span className="bold">User:</span> {polls[pollId].user}</div>

                                <div className="buttons-wrapper">
                                    <Button
                                        className="edit-button"
                                        variant="contained"
                                        onClick={() => setModal({
                                            ...modal,
                                            open: true,
                                            create: false,
                                            name: polls[pollId].name,
                                            url: polls[pollId].url,
                                            poll: polls[pollId]
                                        })}>
                                        Edit
                                    </Button>

                                    <Button
                                        className="delete-button"
                                        variant="contained"
                                        onClick={() => onDeletePoll(polls[pollId])}>
                                        Delete
                                    </Button>
                                </div>
                            </div>
                        </Card>
                    </Fade>
                ))}
            </div>

            <Dialog onClose={resetModal} open={modal.open}>
                <DialogTitle>{modal.create ? "Add Poll" : "Edit Poll"}</DialogTitle>
                <div className="poll-form">
                    <TextField
                        className="poll-form-input"
                        label="Name"
                        variant="outlined"
                        defaultValue={modal.name}
                        onChange={event => setModal({ ...modal, name: event.target.value })} />
                    <TextField
                        className="poll-form-input"
                        label="URL"
                        variant="outlined"
                        defaultValue={modal.url}
                        onChange={event => setModal({ ...modal, url: event.target.value })} />
                    <Button className="save-button" variant="contained" onClick={savePoll}>Save</Button>
                </div>
            </Dialog>

            <Snackbar
                open={snackbar.open}
                autoHideDuration={6000}
                onClose={() => setSnackbar({ open: false, message: "" })}
                message={snackbar.message}
            />
        </Container>
    );
}

export default PollInfo;