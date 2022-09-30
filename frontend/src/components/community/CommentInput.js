import React, { useContext, useState } from 'react';
import { useLocation } from 'react-router-dom';
import UseAxios from '../../utils/UseAxios';
import Context from '../../utils/Context';

function CommentInput(props) {
  const [newComment, setNewComment] = useState('');
  const { userData } = useContext(Context);
  const location = useLocation();
  const onCreate = () => {
    if (newComment.length === 0) {
      alert('글자를 입력해주세요.');
    } else {
      const communityCommentRequestDTO = {
        content: newComment,
        isComment: true,
        parentId: 0,
      };
      UseAxios.post(
        `/communities/stars/${location.state.starId}/boards/${location.state.boardId}/comments`,
        communityCommentRequestDTO
      )
        .then((res) => {
          console.log(res);
          setNewComment('');
        })
        .catch((err) => console.log(err));

      //   props.setCommentData(props.commentData.concat(comment));
    }
  };
  return (
    <div style={{ display: 'flex', alignItems: 'center', marginBottom: 15 }}>
      <div
        style={{
          height: 35,
        }}
      >
        <img
          alt='star'
          className={props.styles.commentImg}
          src={`files/${userData.profileImg}`}
        ></img>
      </div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          borderStyle: 'solid',
          flex: 1,
          borderWidth: 0,
          borderBottomWidth: 0.3,
          padding: 8,
          marginLeft: 10,
        }}
      >
        <div style={{ flex: 1, marginRight: 15 }}>
          <input
            className={props.styles.inputBox}
            value={newComment}
            onChange={(e) => setNewComment(e.target.value.replace(/^\s*/, ''))}
            type={'text'}
            placeholder={'댓글 추가...'}
          ></input>
        </div>
        <div style={{ color: '#7E370C', fontSize: 14 }} onClick={onCreate}>
          게시
        </div>
      </div>
    </div>
  );
}

export default CommentInput;
