/*
 * mxisd - Matrix Identity Server Daemon
 * Copyright (C) 2017 Kamax Sarl
 *
 * https://www.kamax.io/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.kamax.mxisd.threepid.generator;

import io.kamax.matrix.ThreePid;
import io.kamax.mxisd.as.IMatrixIdInvite;
import io.kamax.mxisd.config.MatrixConfig;
import io.kamax.mxisd.config.ServerConfig;
import io.kamax.mxisd.http.IsAPIv1;
import io.kamax.mxisd.invitation.IThreePidInviteReply;
import io.kamax.mxisd.threepid.session.IThreePidSession;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import static io.kamax.mxisd.http.io.identity.StoreInviteRequest.Keys.RoomName;
import static io.kamax.mxisd.http.io.identity.StoreInviteRequest.Keys.SenderDisplayName;

public abstract class PlaceholderNotificationGenerator {

    private MatrixConfig mxCfg;
    private ServerConfig srvCfg;

    public PlaceholderNotificationGenerator(MatrixConfig mxCfg, ServerConfig srvCfg) {
        this.mxCfg = mxCfg;
        this.srvCfg = srvCfg;
    }

    protected String populateForCommon(ThreePid recipient, String input) {
        String domainPretty = WordUtils.capitalizeFully(mxCfg.getDomain());

        return input
                .replace("%DOMAIN%", mxCfg.getDomain())
                .replace("%DOMAIN_PRETTY%", domainPretty)
                .replace("%RECIPIENT_MEDIUM%", recipient.getMedium())
                .replace("%RECIPIENT_ADDRESS%", recipient.getAddress());
    }

    protected String populateForInvite(IMatrixIdInvite invite, String input) {
        String senderName = invite.getProperties().getOrDefault(SenderDisplayName, "");
        String senderNameOrId = StringUtils.defaultIfBlank(senderName, invite.getSender().getId());
        String roomName = invite.getProperties().getOrDefault(RoomName, "");
        String roomNameOrId = StringUtils.defaultIfBlank(roomName, invite.getRoomId());

        return populateForCommon(new ThreePid(invite.getMedium(), invite.getAddress()), input)
                .replace("%SENDER_ID%", invite.getSender().getId())
                .replace("%SENDER_NAME%", senderName)
                .replace("%SENDER_NAME_OR_ID%", senderNameOrId)
                .replace("%RECIPIENT_ID%", invite.getInvitee().getId())
                .replace("%ROOM_ID%", invite.getRoomId())
                .replace("%ROOM_NAME%", roomName)
                .replace("%ROOM_NAME_OR_ID%", roomNameOrId);
    }

    protected String populateForReply(IThreePidInviteReply invite, String input) {
        ThreePid tpid = new ThreePid(invite.getInvite().getMedium(), invite.getInvite().getAddress());

        String senderName = invite.getInvite().getProperties().getOrDefault(SenderDisplayName, "");
        String senderNameOrId = StringUtils.defaultIfBlank(senderName, invite.getInvite().getSender().getId());
        String roomName = invite.getInvite().getProperties().getOrDefault(RoomName, "");
        String roomNameOrId = StringUtils.defaultIfBlank(roomName, invite.getInvite().getRoomId());

        return populateForCommon(tpid, input)
                .replace("%SENDER_ID%", invite.getInvite().getSender().getId())
                .replace("%SENDER_NAME%", senderName)
                .replace("%SENDER_NAME_OR_ID%", senderNameOrId)
                .replace("%INVITE_MEDIUM%", tpid.getMedium())
                .replace("%INVITE_ADDRESS%", tpid.getAddress())
                .replace("%ROOM_ID%", invite.getInvite().getRoomId())
                .replace("%ROOM_NAME%", roomName)
                .replace("%ROOM_NAME_OR_ID%", roomNameOrId);
    }

    protected String populateForValidation(IThreePidSession session, String input) {
        String validationLink = srvCfg.getPublicUrl() + IsAPIv1.getValidate(
                session.getThreePid().getMedium(),
                session.getId(),
                session.getSecret(),
                session.getToken()
        );

        return populateForCommon(session.getThreePid(), input)
                .replace("%VALIDATION_LINK%", validationLink)
                .replace("%VALIDATION_TOKEN%", session.getToken())
                .replace("%NEXT_URL%", validationLink);
    }

    protected String populateForRemoteValidation(IThreePidSession session, String input) {
        return populateForValidation(session, input);
    }

}
