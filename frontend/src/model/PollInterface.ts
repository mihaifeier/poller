import PollStatusEnum from "./enum/PollStatusEnum";

interface PollInterface {
    id: number,
    url: string,
    name: string,
    pollStatus: PollStatusEnum,
    user: string,
    createdAt: string,
    active?: boolean
}

export default PollInterface;