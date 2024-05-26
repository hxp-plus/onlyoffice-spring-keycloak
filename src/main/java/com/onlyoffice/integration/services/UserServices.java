/**
 *
 * (c) Copyright Ascensio System SIA 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.onlyoffice.integration.services;

import com.onlyoffice.integration.entities.User;
import com.onlyoffice.integration.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServices {
    @Autowired
    private UserRepository userRepository;

    // get a list of all users
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // get a user by their ID
    public Optional<User> findUserById(final Integer id) {
        return userRepository.findById(id);
    }

    // create a user with the specified parameters
    public User createUser(final String name, final String email,
                           final List<String> description, final String group,
                           final List<String> reviewGroups,
                           final List<String> viewGroups,
                           final List<String> editGroups,
                           final List<String> removeGroups,
                           final List<String> userInfoGroups, final Boolean favoriteDoc,
                           final Boolean chat,
                           final Boolean protect,
                           final Boolean avatar) {
        User newUser = new User();
        newUser.setName(name);  // set the user name
        newUser.setEmail(email);  // set the user email
        newUser.setAvatar(avatar);

        userRepository.save(newUser); // save a new user

        return newUser;
    }
}
